package com.artseek;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import com.artseek.application.mediator.CommandHandler;
import com.artseek.application.mediator.QueryHandler;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SliceAssignment;
import com.tngtech.archunit.library.dependencies.SliceIdentifier;

@AnalyzeClasses(packages = "com.artseek", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureUnitTests {

    private static final String DOMAIN_PACKAGE = "com.artseek.domain";
    private static final String INFRASTRUCTURE_PACKAGE = "com.artseek.infrastructure";
    private static final String SHARED_DOMAIN_PACKAGE = "shared";

    @ArchTest
    static final ArchRule domainDoesNotDependOnOuterLayersOrSpringWebOrData = noClasses()
            .that().resideInAPackage(DOMAIN_PACKAGE + "..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.artseek.application..",
                    INFRASTRUCTURE_PACKAGE + "..",
                    "org.springframework.web..",
                    "org.springframework.http..",
                    "org.springframework.data..");

    @ArchTest
    static final ArchRule applicationDoesNotDependOnInfrastructure = noClasses()
            .that().resideInAPackage("com.artseek.application..")
            .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE_PACKAGE + "..");

    @ArchTest
    static final ArchRule controllersDispatchThroughTheMediatorInsteadOfHandlersOrRepositories = noClasses()
            .that().areAnnotatedWith(RestController.class)
            .or().areAnnotatedWith(Controller.class)
            .should().dependOnClassesThat(assignableTo(QueryHandler.class)
                    .or(assignableTo(CommandHandler.class))
                    .or(assignableTo(org.springframework.data.repository.Repository.class))
                    .or(resideInAPackage("..repository..")));

    @ArchTest
    static void aggregateInfrastructureDoesNotDependOnAnotherAggregateInfrastructure(JavaClasses classes) {
        slices().assignedFrom(new AggregateInfrastructureSlices(domainAggregatesOf(classes)))
                .should().notDependOnEachOther()
                .check(classes);
    }

    private static Set<String> domainAggregatesOf(JavaClasses classes) {
        return classes.stream()
                .map(JavaClass::getPackageName)
                .filter(packageName -> packageName.startsWith(DOMAIN_PACKAGE + "."))
                .map(packageName -> firstSubpackage(packageName, DOMAIN_PACKAGE))
                .filter(aggregate -> !aggregate.equals(SHARED_DOMAIN_PACKAGE))
                .collect(Collectors.toUnmodifiableSet());
    }

    private static String firstSubpackage(String packageName, String parentPackage) {
        return packageName.substring(parentPackage.length() + 1).split("\\.")[0];
    }

    /**
     * Assigns each class under {@code infrastructure.<aggregate>} to that aggregate's slice and ignores
     * cross-cutting infrastructure such as config, mediator, bootstrap and api.
     */
    private record AggregateInfrastructureSlices(Set<String> aggregates) implements SliceAssignment {

        @Override
        public SliceIdentifier getIdentifierOf(JavaClass javaClass) {
            String packageName = javaClass.getPackageName();
            if (!packageName.startsWith(INFRASTRUCTURE_PACKAGE + ".")) {
                return SliceIdentifier.ignore();
            }
            String candidate = firstSubpackage(packageName, INFRASTRUCTURE_PACKAGE);
            return aggregates.contains(candidate) ? SliceIdentifier.of(candidate) : SliceIdentifier.ignore();
        }

        @Override
        public String getDescription() {
            return "infrastructure packages of domain aggregates " + aggregates;
        }
    }
}
