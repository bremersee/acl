/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.acl.spring.data.mongodb;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bremersee.acl.Ace;
import org.bremersee.acl.Acl;
import org.bremersee.acl.annotation.AclHolder;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.util.Assert;

/**
 * The acl index operations.
 *
 * @author Christian Bremer
 */
public class AclIndexOperations {

  private final MongoOperations mongoOperations;

  /**
   * Instantiates a new acl index operations.
   *
   * @param mongoOperations the mongo operations
   */
  public AclIndexOperations(MongoOperations mongoOperations) {
    Assert.notNull(mongoOperations, "Mongo operations must be present.");
    this.mongoOperations = mongoOperations;
  }

  /**
   * Gets acl index info.
   *
   * @param entityClass the entity class
   * @return the acl index info
   */
  public List<IndexInfo> getAclIndexInfo(Class<?> entityClass) {
    Assert.notNull(entityClass, "Entity class must be present.");
    String aclPath = getAclPath(entityClass);
    return getAclIndexInfo(entityClass, aclPath);
  }

  /**
   * Gets acl index info.
   *
   * @param entityClass the entity class
   * @param aclPath the acl path
   * @return the acl index info
   */
  public List<IndexInfo> getAclIndexInfo(Class<?> entityClass, String aclPath) {
    Assert.notNull(entityClass, "Entity class must be present.");
    return getAclIndexInfo(mongoOperations.indexOps(entityClass), aclPath);
  }

  /**
   * Gets acl index info.
   *
   * @param collectionName the collection name
   * @param aclPath the acl path
   * @return the acl index info
   */
  public List<IndexInfo> getAclIndexInfo(String collectionName, String aclPath) {
    Assert.hasLength(collectionName, "Collection name must be present.");
    return getAclIndexInfo(mongoOperations.indexOps(collectionName), aclPath);
  }

  private List<IndexInfo> getAclIndexInfo(IndexOperations indexOps, String aclPath) {
    String validAclPath = isEmpty(aclPath) ? "" : aclPath.trim() + ".";
    // Example: acl.(owner|(entries.(\.*).(guest|users|roles|groups)))(.*)
    String regex = String.format(
        "%s(%s|(%s.(.*).(%s|%s|%s|%s)))(.*)",
        validAclPath, Acl.OWNER, Acl.ENTRIES, Ace.GUEST, Ace.USERS, Ace.ROLES, Ace.GROUPS);
    Pattern pattern = Pattern.compile(regex);
    return indexOps.getIndexInfo()
        .stream()
        .filter(indexInfo -> pattern.matcher(indexInfo.getName()).matches())
        .collect(Collectors.toList());
  }

  /**
   * Ensure acl indexes.
   *
   * @param entityClass the entity class
   * @param possiblePermissions the possible permissions
   * @param dropIndexesOfOtherPermissions the drop indexes of other permissions
   */
  public void ensureAclIndexes(
      Class<?> entityClass,
      Collection<String> possiblePermissions,
      boolean dropIndexesOfOtherPermissions) {

    Assert.notNull(entityClass, "Entity class must be present.");
    String aclPath = getAclPath(entityClass);
    ensureAclIndexes(entityClass, aclPath, possiblePermissions, dropIndexesOfOtherPermissions);
  }

  /**
   * Ensure acl indexes.
   *
   * @param entityClass the entity class
   * @param aclPath the acl path
   * @param possiblePermissions the possible permissions
   * @param dropIndexesOfOtherPermissions the drop indexes of other permissions
   */
  public void ensureAclIndexes(
      Class<?> entityClass,
      String aclPath,
      Collection<String> possiblePermissions,
      boolean dropIndexesOfOtherPermissions) {

    Assert.notNull(entityClass, "Entity class must be present.");
    ensureAclIndexes(
        mongoOperations.indexOps(entityClass),
        aclPath,
        possiblePermissions,
        dropIndexesOfOtherPermissions);
  }

  /**
   * Ensure acl indexes.
   *
   * @param collectionName the collection name
   * @param aclPath the acl path
   * @param possiblePermissions the possible permissions
   * @param dropIndexesOfOtherPermissions the drop indexes of other permissions
   */
  public void ensureAclIndexes(
      String collectionName,
      String aclPath,
      Collection<String> possiblePermissions,
      boolean dropIndexesOfOtherPermissions) {

    Assert.hasLength(collectionName, "Collection name must be present.");
    ensureAclIndexes(
        mongoOperations.indexOps(collectionName.trim()),
        aclPath,
        possiblePermissions,
        dropIndexesOfOtherPermissions);
  }

  private void ensureAclIndexes(
      IndexOperations indexOps,
      String aclPath,
      Collection<String> possiblePermissions,
      boolean dropIndexesOfOtherPermissions) {

    if (dropIndexesOfOtherPermissions) {
      dropUnusedAclIndexes(indexOps, aclPath, possiblePermissions);
    }

    String validAclPath = isEmpty(aclPath) ? "" : aclPath.trim();
    indexOps.ensureIndex(new Index()
        .on(path(validAclPath, Acl.OWNER), Direction.ASC));
    if (!isEmpty(possiblePermissions)) {
      List.of(Ace.GUEST, Ace.USERS, Ace.ROLES, Ace.GROUPS)
          .forEach(field -> Set.copyOf(possiblePermissions)
              .forEach(permission -> indexOps
                  .ensureIndex(new Index()
                      .on(path(validAclPath, Acl.ENTRIES, permission, field),
                          Direction.ASC))));
    }
  }

  private void dropUnusedAclIndexes(
      IndexOperations indexOps,
      String aclPath,
      Collection<String> possiblePermissions) {

    Set<String> newPermissions = isEmpty(possiblePermissions)
        ? Set.of()
        : Set.copyOf(possiblePermissions);
    String validAclPath = isEmpty(aclPath) ? "" : aclPath.trim() + ".";
    // Example: acl.entries.(\.*).(guest|users|roles|groups)(.*)
    String regex = String.format(
        "%s%s.(.*).(%s|%s|%s|%s)(.*)",
        validAclPath, Acl.ENTRIES, Ace.GUEST, Ace.USERS, Ace.ROLES, Ace.GROUPS);
    Pattern pattern = Pattern.compile(regex);
    getAclIndexInfo(indexOps, aclPath)
        .stream()
        .filter(indexInfo -> {
          Matcher matcher = pattern.matcher(indexInfo.getName());
          return matcher.matches() && !newPermissions.contains(matcher.group(1));
        })
        .forEach(indexInfo -> indexOps.dropIndex(indexInfo.getName()));
  }

  private String getAclPath(Class<?> entityClass) {
    return Optional
        .ofNullable(findAnnotation(entityClass, AclHolder.class))
        .map(AclHolder::path)
        .orElseThrow(() -> new IllegalArgumentException(String
            .format(
                "Entity class %s must be annotated with %s.",
                entityClass.getSimpleName(), AclHolder.class.getSimpleName())));
  }

  private String path(String... pathSegments) {
    String aclPath = String.join(".", pathSegments);
    return aclPath.startsWith(".") ? aclPath.substring(1) : aclPath;
  }

}
