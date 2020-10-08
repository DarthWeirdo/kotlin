/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.frontend.fir.handlers

import org.jetbrains.kotlin.test.frontend.fir.FirFrontendResults
import org.jetbrains.kotlin.test.model.AllModulesAnalysisHandler
import org.jetbrains.kotlin.test.model.FrontendResultsHandler

abstract class FirAnalysisHandler<in S> : FrontendResultsHandler<FirFrontendResults, S>()

abstract class FirAllModulesAnalysisHandler<S> : AllModulesAnalysisHandler<FirFrontendResults, S, FirAnalysisHandler<S>>()
