/*
 * Copyright 2019-2019 Gryphon Zone
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zone.gryphon.dropwizard.testing;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VerboseRule extends ExternalResource {

    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    private static final Logger logger = LoggerFactory.getLogger("com.fake.verbose");

    @Override
    protected void after() {
        appender.start();
        ((ch.qos.logback.classic.Logger) logger).addAppender(appender);

        logger.debug("[{}] log message from verbose test rule", getClass().getSimpleName());
    }

    public List<ILoggingEvent> getRecordedLogs() {
        return appender.list;
    }
}
