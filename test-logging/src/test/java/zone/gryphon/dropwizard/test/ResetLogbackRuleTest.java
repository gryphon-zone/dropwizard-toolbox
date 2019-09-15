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


import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import zone.gryphon.dropwizard.testing.RepeatedRule;
import zone.gryphon.dropwizard.testing.VerboseRule;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ResetLogbackRuleTest {

    public static class TestApplication extends Application<Configuration> {

        public TestApplication() {

        }

        @Override
        public void run(Configuration configuration, Environment environment) {
            environment.healthChecks().register("fake", new HealthCheck() {
                @Override
                protected Result check() {
                    return Result.healthy("fake");
                }
            });

        }
    }

    private static class NoopStatement extends Statement {

        @Override
        public void evaluate() {

        }
    }

    /**
     * Run all tests multiple times, to decrease likelihood test order matters
     * (a risk due to logging being statically configured, meaning tests can interfere with each other).
     * <p>
     * Note that this must be installed as a {@link ClassRule} so that the repetition happens at the test class level,
     * and not at the individual test level.
     */
    @ClassRule
    public static final RepeatedRule repeat = new RepeatedRule(2);

    private final ResetLogbackRule reset = new ResetLogbackRule();

    private final VerboseRule verbose = new VerboseRule();

    private final DropwizardAppRule<Configuration> dropwizard = new DropwizardAppRule<>(
            TestApplication.class,
            null,
            ConfigOverride.config("server.applicationConnectors[0].port", "0"));

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private Description description;

    private void runChain(RuleChain chain) {
        try {
            chain.apply(new NoopStatement(), description).evaluate();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Test
    public void testWithReset() {
        runChain(RuleChain.outerRule(RuleChain.emptyRuleChain())
                .around(verbose)
                .around(reset)
                .around(dropwizard));

        assertThat(verbose.getRecordedLogs()).isEmpty();
    }

    @Test
    public void testWithoutReset() {
        runChain(RuleChain.outerRule(RuleChain.emptyRuleChain())
                .around(verbose)
                .around(dropwizard));

        assertThat(verbose.getRecordedLogs()).hasSize(1);
    }

}
