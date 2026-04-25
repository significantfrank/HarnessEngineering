package com.harness.crm;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * COLA分层架构守护测试
 * 确保项目遵循COLA（Clean Object-oriented and Layered Architecture）分层依赖规则
 *
 * COLA分层依赖方向：
 * adapter → app → domain ← infrastructure
 *
 * 规则说明：
 * - adapter层可依赖app层和domain层（允许引用domain枚举作为请求参数）
 * - app层只能依赖domain层
 * - domain层不依赖任何其他业务层（adapter/app/infrastructure）
 * - infrastructure层只能依赖domain层（实现gateway接口）
 */
class ColaArchitectureTest {

    static JavaClasses importedClasses;

    @BeforeAll
    static void importClasses() {
        importedClasses = new ClassFileImporter()
                .importPackages("com.harness.crm");
    }

    @Nested
    @DisplayName("分层依赖规则")
    class LayerDependencyRules {

        @Test
        @DisplayName("domain层不依赖adapter层")
        void domainShouldNotDependOnAdapter() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..adapter..")
                    .because("domain层是核心层，不应依赖adapter层");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("domain层不依赖app层")
        void domainShouldNotDependOnApp() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..app..")
                    .because("domain层是核心层，不应依赖app层");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("domain层不依赖infrastructure层")
        void domainShouldNotDependOnInfrastructure() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..infrastructure..")
                    .because("domain层通过gateway接口实现依赖倒置，不应直接依赖infrastructure层");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("app层不依赖adapter层")
        void appShouldNotDependOnAdapter() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..app..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..adapter..")
                    .because("app层不应依赖adapter层，依赖方向应为adapter→app");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("adapter层不依赖infrastructure层")
        void adapterShouldNotDependOnInfrastructure() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..adapter..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..infrastructure..")
                    .because("adapter层不应直接访问infrastructure层，应通过app层间接使用");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("infrastructure层不依赖adapter层")
        void infrastructureShouldNotDependOnAdapter() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..infrastructure..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..adapter..")
                    .because("infrastructure层不应依赖adapter层");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("infrastructure层不依赖app层")
        void infrastructureShouldNotDependOnApp() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..infrastructure..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..app..")
                    .because("infrastructure层只实现domain层的gateway接口，不应依赖app层");

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("命名规范规则")
    class NamingConventionRules {

        @Test
        @DisplayName("Controller类应位于adapter包并以Controller结尾")
        void controllerNaming() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..adapter.web..")
                    .and().areNotNestedClasses()
                    .and().haveSimpleNameNotEndingWith("Test")
                    .and().haveSimpleNameNotEndingWith("Response")
                    .and().haveSimpleNameNotEndingWith("ExceptionHandler")
                    .and().haveSimpleNameNotContaining("Api")
                    .should().haveSimpleNameEndingWith("Controller")
                    .because("adapter层的控制器类应以Controller结尾");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("domain层gateway接口应以GatewayI结尾")
        void gatewayInterfaceNaming() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..domain..gateway..")
                    .should().haveSimpleNameEndingWith("GatewayI")
                    .because("domain层的gateway接口应以GatewayI结尾，I表示接口");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("domain层实体类应以Entity结尾")
        void entityNaming() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..domain..entity..")
                    .and().areNotNestedClasses()
                    .should().haveSimpleNameEndingWith("Entity")
                    .because("domain层的实体类应以Entity结尾");

            rule.check(importedClasses);
        }


        @Test
        @DisplayName("infrastructure层Repository接口应以Repository结尾")
        void repositoryNaming() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..infrastructure..repository..")
                    .should().haveSimpleNameEndingWith("Repository")
                    .because("infrastructure层的Repository接口应以Repository结尾");

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("依赖倒置规则")
    class DependencyInversionRules {

        @Test
        @DisplayName("infrastructure层gateway实现类必须实现domain层gateway接口")
        void gatewayImplShouldImplementGatewayInterface() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..infrastructure..gateway..")
                    .and().haveSimpleNameEndingWith("Impl")
                    .and().areNotAnonymousClasses()
                    .should().implement(resideInAPackage("..domain..gateway.."))
                    .because("infrastructure层的gateway实现必须实现domain层定义的gateway接口，遵循依赖倒置原则");

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("包分层规则")
    class PackageLayeringRules {

        @Test
        @DisplayName("不许分层之间出现循环依赖")
        void noPackageCycles() {
            ArchRule rule = slices()
                    .matching("com.harness.crm.(*)..")
                    .should().beFreeOfCycles()
                    .because("COLA分层架构不允许出现循环依赖");

            rule.check(importedClasses);
        }
    }

    private static com.tngtech.archunit.base.DescribedPredicate<com.tngtech.archunit.core.domain.JavaClass> resideInAPackage(String packageIdentifier) {
        return com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage(packageIdentifier);
    }
}
