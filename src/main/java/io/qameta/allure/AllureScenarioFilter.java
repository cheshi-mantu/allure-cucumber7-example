package io.qameta.allure;

import io.qameta.allure.testfilter.FileTestPlanSupplier;
import io.qameta.allure.testfilter.TestPlan;
import io.qameta.allure.testfilter.TestPlanV1_0;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.launcher.PostDiscoveryFilter;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AllureScenarioFilter implements PostDiscoveryFilter {

    private static final String SEGMENT_TYPE_ENGINE = "engine";
    private static final String SEGMENT_VALUE_CUCUMBER = "cucumber";

    private static final Pattern ID_TAG = Pattern.compile("^@?allure\\.id[:=](?<id>.+)$");

    private final TestPlan testPlan;

    public AllureScenarioFilter() {
        this(new FileTestPlanSupplier().supply().orElse(null));
    }

    public AllureScenarioFilter(TestPlan testPlan) {
        this.testPlan = testPlan;
    }

    @Override
    public FilterResult apply(final TestDescriptor descriptor) {
        if (!isCucumberEngine(descriptor)) {
            return FilterResult.included("filter applied only for cucumber engine");
        }
        if (!descriptor.getChildren().isEmpty()) {
            return FilterResult.included("filter applied only for cucumber scenario");
        }
        final Optional<TestDescriptor> possibleParentDescriptor = descriptor.getParent();
        if (!possibleParentDescriptor.isPresent()) {
            return FilterResult.included("filter only applied for test with parent");
        }
        final TestDescriptor parentDescriptor = possibleParentDescriptor.get();
        final String allureId = findAllureId(descriptor.getTags());
        final String uniqueId = descriptor.getUniqueId().toString();
        final String fullName = String.format("%s: %s", parentDescriptor.getDisplayName(), descriptor.getDisplayName());
        return FilterResult.includedIf(isIncluded(testPlan, allureId, uniqueId, fullName));
    }

    public boolean isCucumberEngine(final TestDescriptor descriptor) {
        return descriptor.getUniqueId().getSegments().stream()
                .filter(s -> SEGMENT_TYPE_ENGINE.equals(s.getType()))
                .anyMatch(s -> SEGMENT_VALUE_CUCUMBER.equals(s.getValue()));
    }

    private boolean isIncluded(final TestPlan testPlan,
                               final String allureId,
                               final String uniqueId,
                               final String fullName) {
        if (testPlan instanceof TestPlanV1_0) {
            final TestPlanV1_0 tp = (TestPlanV1_0) testPlan;
            return Objects.isNull(tp.getTests()) || tp.getTests()
                    .stream()
                    .filter(Objects::nonNull)
                    .anyMatch(tc -> match(tc, allureId, uniqueId, fullName));
        }
        return true;
    }

    @SuppressWarnings("BooleanExpressionComplexity")
    private boolean match(final TestPlanV1_0.TestCase tc,
                          final String allureId,
                          final String uniqueId,
                          final String fullName) {
        return Objects.nonNull(tc.getId()) && tc.getId().equals(allureId)
                || Objects.nonNull(tc.getSelector()) && tc.getSelector().equals(uniqueId)
                || Objects.nonNull(tc.getSelector()) && tc.getSelector().equals(fullName);
    }


    private String findAllureId(final Collection<TestTag> tags) {
        return tags.stream()
                .map(TestTag::getName)
                .filter(Objects::nonNull)
                .map(t -> {
                    final Matcher matcher = ID_TAG.matcher(t);
                    return matcher.matches()
                            ? Optional.ofNullable(matcher.group("id"))
                            : Optional.<String>empty();
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny()
                .orElse(null);
    }
}
