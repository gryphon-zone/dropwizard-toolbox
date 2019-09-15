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
 * When used as a test/class rule, {@link io.dropwizard.testing.junit.DropwizardAppRule}
 * the {@link io.dropwizard.testing.DropwizardTestSupport#after()} method while cleaning up.
 * <p>
 * This in turn ends up
 * <a href="https://github.com/dropwizard/dropwizard/blob/v1.3.14/dropwizard-testing/src/main/java/io/dropwizard/testing/DropwizardTestSupport.java#L180-L181">
 * resetting the Logback configuration factory.
 * </a>
 * <p>
 * If Logback was configured before the {@link io.dropwizard.testing.junit.DropwizardAppRule} was started, that
 * configuration is lost, and instead it uses the Dropwizard default logging configuration.
 * <p>
 * This has the potential to cause issues if there are additional test/class rules which need to be shut down, since
 * the default Dropwizard logging configuration may cause overly verbose output for these rules.
 * <p>
 * This rule re-runs the initial Logback configuration setup when the {@link ResetLogbackRule#after()} method is called,
 * meaning that Logback will be set back to the state it was in before the
 * {@link io.dropwizard.testing.junit.DropwizardAppRule} ran.
 * <p>
 * Typical usage will look something like the following:
 * <pre>{@code
 * public class MyTestClass {
 *
 *   private static final VerboseTestRule verboseTestRule = ...;
 *
 *   private static final ResetLogbackRule resetLogbackRule = ...;
 *
 *   private static final DropwizardAppRule dropwizardAppRule = ...;
 *
 *   @ClassRule
 *   public static RuleChain chain = RuleChain.outerRule(RuleChain.emptyRuleChain())
 *       .around(verboseTestRule)
 *       .around(resetLogbackRule)
 *       .around(dropwizardAppRule);
 *
 *   @Test
 *   public void myTest() {
 *       ...
 *   }
 * }
 * }</pre>
 * This will cause the {@link ResetLogbackRule#after()} method to run after Dropwizard's completes, but before
 * the {@code after()} method on the rest rule with verbose shutdown code runs.
 *
 * @author gschmidt
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
