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

package zone.gryphon.dropwizard.test;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import org.junit.rules.ExternalResource;
import org.slf4j.LoggerFactory;

/**
 * Utility to reset Logback after a {@code DropwizardAppRule} completes.
 * <p>
 * When used as a test/class rule, {@link io.dropwizard.testing.junit.DropwizardAppRule} calls
 * {@link io.dropwizard.testing.DropwizardTestSupport#after()}
 * <a href="https://github.com/dropwizard/dropwizard/blob/v1.2.0/dropwizard-testing/src/main/java/io/dropwizard/testing/junit/DropwizardAppRule.java#L161">
 * during its own {@code after()} method.
 * </a>
 * <p>
 * {@link io.dropwizard.testing.DropwizardTestSupport#after()} in turn
 * <a href="https://github.com/dropwizard/dropwizard/blob/v1.2.0/dropwizard-testing/src/main/java/io/dropwizard/testing/DropwizardTestSupport.java#L170-L171">
 * resets the Logback configuration
 * </a>
 * (assuming Logback is the logging framework in use).
 * <p>
 * However, that call runs the default <i>Dropwizard</i> logging setup, not the default <i>Logback</i>
 * logging setup, meaning that if Logback was configured externally prior to launching Dropwizard
 * (e.g. to control logging in other {@link org.junit.rules.TestRule}), that configuration will not be restored.
 * <p>
 * This has the potential to cause issues if there are additional test/class rules which are shut down after
 * the {@link io.dropwizard.testing.junit.DropwizardAppRule}, since the default Dropwizard logging configuration
 * may cause them to be overly verbose.
 * <p>
 * This rule re-initializes Logback when {@link ResetLogbackRule#after()} is called,
 * meaning that Logback will be set back to the state it was in before the
 * {@link io.dropwizard.testing.junit.DropwizardAppRule} ran (assuming Logback was not programmatically configured).
 * <p>
 * Typical usage will look something like the following:
 * <pre>
 * public class MyTestClass {
 *
 *   private static final VerboseTestRule verboseTestRule = ...;
 *
 *   private static final ResetLogbackRule resetLogbackRule = ...;
 *
 *   private static final DropwizardAppRule dropwizardAppRule = ...;
 *
 *   &#064;ClassRule
 *   public static RuleChain chain = RuleChain.outerRule(RuleChain.emptyRuleChain())
 *       .around(verboseTestRule)
 *       .around(resetLogbackRule)
 *       .around(dropwizardAppRule);
 *
 *   &#064;Test
 *   public void myTest() {
 *       ...
 *   }
 * }
 * </pre>
 * This will cause the {@link ResetLogbackRule#after()} method to run after Dropwizard's completes, but before
 * the {@code after()} method on the test rule with verbose shutdown code runs.
 *
 * @author tyrol
 */
public class ResetLogbackRule extends ExternalResource {

    @Override
    protected void after() {
        try {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            context.reset();
            new ContextInitializer(context).autoConfig();
        } catch (Exception e) {
            // Since configuring the logging context failed, calls to slf4j may not be logged anywhere.
            // Thus, write to stderr to make sure the message isn't suppressed.
            System.err.println("Failed to reconfigure logging context");
            e.printStackTrace(System.err);
        }
    }
}
