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

package org.bremersee.acl.spring.boot.autoconfigure.data.mongo;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bremersee.acl.spring.data.mongodb.convert.AclConverters;
import org.bremersee.spring.boot.autoconfigure.data.mongo.MongoCustomConversionsProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.ClassUtils;

/**
 * The acl mongo custom conversions provider.
 *
 * @author Christian Bremer
 */
@ConditionalOnClass({AclConverters.class})
@Configuration
@Slf4j
public class AclMongoCustomConversionsProvider implements MongoCustomConversionsProvider {

  /**
   * Init.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("\n"
            + "*********************************************************************************\n"
            + "* {}\n"
            + "*********************************************************************************",
        ClassUtils.getUserClass(getClass()).getSimpleName());
  }

  @Override
  public List<Converter<?, ?>> getCustomConversions() {
    return AclConverters.getConvertersToRegister();
  }
}
