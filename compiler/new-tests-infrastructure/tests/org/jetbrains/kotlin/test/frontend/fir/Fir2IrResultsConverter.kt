/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.frontend.fir

import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.jvm.JvmGeneratorExtensions
import org.jetbrains.kotlin.backend.jvm.JvmIrCodegenFactory
import org.jetbrains.kotlin.backend.jvm.jvmPhases
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.jvm.compiler.NoScopeRecordCliBindingTrace
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.codegen.ClassBuilderFactories
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.container.get
import org.jetbrains.kotlin.fir.backend.jvm.FirJvmBackendClassResolver
import org.jetbrains.kotlin.fir.backend.jvm.FirMetadataSerializer
import org.jetbrains.kotlin.fir.psi
import org.jetbrains.kotlin.ir.descriptors.IrFunctionFactory
import org.jetbrains.kotlin.ir.util.generateTypicalIrProviderList
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.CompilerEnvironment
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory
import org.jetbrains.kotlin.test.backend.ir.IrBackendInputInfo
import org.jetbrains.kotlin.test.model.*
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.kotlinCoreEnvironmentProvider

class Fir2IrResultsConverter(
    testServices: TestServices
) : Frontend2BackendConverter<FirSourceArtifact, IrBackendInputInfo>(
    testServices,
    FrontendKind.FIR,
    BackendKind.IrBackend
) {
    override fun convert(
        module: TestModule,
        frontendResults: ResultingArtifact.Source<FirSourceArtifact>
    ): IrBackendInputInfo {
        require(frontendResults is FirSourceArtifact)
        val (irModuleFragment, symbolTable, sourceManager, components) = frontendResults.firAnalyzerFacade.convertToIr()
        val dummyBindingContext = NoScopeRecordCliBindingTrace().bindingContext

        val environment = testServices.kotlinCoreEnvironmentProvider.getKotlinCoreEnvironment(module)
        val configuration = environment.configuration

        val phaseConfig = configuration.get(CLIConfigurationKeys.PHASE_CONFIG) ?: PhaseConfig(jvmPhases)
        val codegenFactory = JvmIrCodegenFactory(phaseConfig)

        // TODO: handle fir from light tree
        val ktFiles = frontendResults.firFiles.values.mapNotNull { it.psi as KtFile? }

        // Create and initialize the module and its dependencies
        val project = environment.project
        val container = TopDownAnalyzerFacadeForJVM.createContainer(
            project, ktFiles, NoScopeRecordCliBindingTrace(), configuration, environment::createPackagePartProvider,
            ::FileBasedDeclarationProviderFactory, CompilerEnvironment,
            TopDownAnalyzerFacadeForJVM.newModuleSearchScope(project, ktFiles), emptyList()
        )

        val generationState = GenerationState.Builder(
            project, ClassBuilderFactories.BINARIES,
            container.get(), dummyBindingContext, ktFiles,
            configuration
        ).codegenFactory(
            codegenFactory
        ).isIrBackend(
            true
        ).jvmBackendClassResolver(
            FirJvmBackendClassResolver(components)
        ).build()

        irModuleFragment.irBuiltins.functionFactory = IrFunctionFactory(irModuleFragment.irBuiltins, symbolTable)
        val extensions = JvmGeneratorExtensions()
        val irProviders = generateTypicalIrProviderList(
            irModuleFragment.descriptor, irModuleFragment.irBuiltins, symbolTable, extensions = extensions
        )

        return IrBackendInputInfo(
            generationState,
            irModuleFragment,
            symbolTable,
            sourceManager,
            phaseConfig,
            irProviders,
            extensions,
        ) { context, irClass, _, serializationBindings, parent ->
            FirMetadataSerializer(frontendResults.session, context, irClass, serializationBindings, parent)
        }
    }
}
