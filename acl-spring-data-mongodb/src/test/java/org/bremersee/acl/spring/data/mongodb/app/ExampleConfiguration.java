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

package org.bremersee.acl.spring.data.mongodb.app;

import org.bremersee.acl.spring.data.mongodb.convert.AclConverters;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * The example configuration.
 *
 * @author Christian Bremer
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = {ExampleConfiguration.class})
@EnableMongoRepositories(basePackageClasses = {ExampleEntityRepository.class})
public class ExampleConfiguration {

  /**
   * Mongo custom conversions.
   *
   * @return the mongo custom conversions
   */
  @Primary
  @Bean
  public MongoCustomConversions customConversions() {
    return new MongoCustomConversions(new AclConverters().getCustomConversions());
  }

}
