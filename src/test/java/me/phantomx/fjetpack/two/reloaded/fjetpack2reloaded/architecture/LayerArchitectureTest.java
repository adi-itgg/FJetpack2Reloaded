package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

/**
 * The layer rules, as tests.
 * <p>
 * The packages form a strict chain: {@code domain} &lt;- {@code application} &lt;-
 * {@code infrastructure} &lt;- {@code presentation}. Nothing may point back inwards, so a use case
 * can never reach a YAML loader, an NBT codec or a Bukkit listener by accident.
 * <p>
 * The inner two layers may use Bukkit types: a Minecraft plugin cannot keep its use cases free of
 * {@code ItemStack} and {@code Player} without inventing a domain for every one of them. What matters
 * is that they cannot reach the outer rings directly - they go through the ports in
 * {@code application.port}.
 */
@AnalyzeClasses(packages = "me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded")
class LayerArchitectureTest {

    @BeforeAll
    static void failLoudlyWhenNothingIsImported() {
        if (new ClassFileImporter().importPackages(
                "me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded").isEmpty())
            throw new AssertionError("no classes imported - every rule below would pass vacuously");
    }

    @ArchTest
    static final ArchRule domainDependsOnNoOtherRing = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..application..", "..infrastructure..", "..presentation..", "..exception..", "..di..");

    @ArchTest
    static final ArchRule domainStaysFrameworkFree = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.bukkit..", "org.spigotmc..", "com.bgsoftware..", "me.ryanhamshire..");

    @ArchTest
    static final ArchRule applicationNeverReachesTheOuterRings = noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..presentation..");

    @ArchTest
    static final ArchRule infrastructureNeverReachesPresentation = noClasses().that().resideInAPackage("..infrastructure..")
            .should().dependOnClassesThat().resideInAPackage("..presentation..");

    @ArchTest
    static final ArchRule applicationKeepsNoMutableStatics = noFields()
            .that().areDeclaredInClassesThat().resideInAPackage("..application..")
            .should().beStatic().andShould().notBeFinal();

}
