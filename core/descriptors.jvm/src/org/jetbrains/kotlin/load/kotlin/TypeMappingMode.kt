/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.load.kotlin

import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.Variance

class TypeMappingMode private constructor(
        val needPrimitiveBoxing: Boolean = true,
        val needInlineClassWrapping: Boolean = false,
        val isForAnnotationParameter: Boolean = false,
        // Here DeclarationSiteWildcards means wildcard generated because of declaration-site variance
        val skipDeclarationSiteWildcards: Boolean = false,
        val skipDeclarationSiteWildcardsIfPossible: Boolean = false,
        private val genericArgumentMode: TypeMappingMode? = null,
        val kotlinCollectionsToJavaCollections: Boolean = true,
        private val genericContravariantArgumentMode: TypeMappingMode? = genericArgumentMode,
        private val genericInvariantArgumentMode: TypeMappingMode? = genericArgumentMode
) {
    companion object {
        /**
         * kotlin.Int is mapped to Ljava/lang/Integer;
         */
        @JvmField
        val GENERIC_ARGUMENT = TypeMappingMode()

        /**
         * see KotlinTypeMapper.forceBoxedReturnType()
         * This configuration should be called only for method return type
         */
        @JvmField
        val RETURN_TYPE_BOXED = TypeMappingMode(needInlineClassWrapping = true)

        /**
         * kotlin.Int is mapped to I
         */
        @JvmField
        val DEFAULT = TypeMappingMode(genericArgumentMode = GENERIC_ARGUMENT, needPrimitiveBoxing = false)

        /**
         * kotlin.Int is mapped to I
         * inline class Foo(val x: Int) is mapped to LFoo;
         * but in signature fun bar(f: Foo), Foo is mapped to I
         */
        @JvmField
        val CLASS_DECLARATION = TypeMappingMode(
            genericArgumentMode = GENERIC_ARGUMENT,
            needPrimitiveBoxing = false,
            needInlineClassWrapping = true
        )

        /**
         * kotlin.Int is mapped to Ljava/lang/Integer;
         * No projections allowed in immediate arguments
         */
        @JvmField
        val SUPER_TYPE = TypeMappingMode(skipDeclarationSiteWildcards = true, genericArgumentMode = GENERIC_ARGUMENT)

        @JvmField
        val SUPER_TYPE_KOTLIN_COLLECTIONS_AS_IS = TypeMappingMode(
                skipDeclarationSiteWildcards = true,
                genericArgumentMode = GENERIC_ARGUMENT,
                kotlinCollectionsToJavaCollections = false
        )

        /**
         * kotlin.reflect.KClass mapped to java.lang.Class
         * Other types mapped as DEFAULT
         */
        @JvmField
        val VALUE_FOR_ANNOTATION = TypeMappingMode(
                isForAnnotationParameter = true,
                needPrimitiveBoxing = false,
                genericArgumentMode = TypeMappingMode(isForAnnotationParameter = true, genericArgumentMode = GENERIC_ARGUMENT))


        @JvmStatic
        fun getModeForReturnTypeNoGeneric(
                isAnnotationMethod: Boolean
        ) = if (isAnnotationMethod) VALUE_FOR_ANNOTATION else DEFAULT

        @JvmStatic
        fun getOptimalModeForValueParameter(
                type: KotlinType
        ) = getOptimalModeForSignaturePart(type, isForAnnotationParameter = false, canBeUsedInSupertypePosition = true)

        @JvmStatic
        fun getOptimalModeForReturnType(
                type: KotlinType,
                isAnnotationMethod: Boolean
        ) = getOptimalModeForSignaturePart(type, isForAnnotationParameter = isAnnotationMethod, canBeUsedInSupertypePosition = false)

        private fun getOptimalModeForSignaturePart(
                type: KotlinType,
                isForAnnotationParameter: Boolean,
                canBeUsedInSupertypePosition: Boolean
        ): TypeMappingMode {
            if (type.arguments.isEmpty()) return DEFAULT

            val contravariantArgumentMode =
                if (!canBeUsedInSupertypePosition)
                    TypeMappingMode(
                            isForAnnotationParameter = isForAnnotationParameter,
                            skipDeclarationSiteWildcards = false,
                            skipDeclarationSiteWildcardsIfPossible = true)
                else
                    null

            val invariantArgumentMode =
                    if (canBeUsedInSupertypePosition)
                        getOptimalModeForSignaturePart(type, isForAnnotationParameter, canBeUsedInSupertypePosition = false)
                    else
                        null

            return TypeMappingMode(
                    isForAnnotationParameter = isForAnnotationParameter,
                    skipDeclarationSiteWildcards = !canBeUsedInSupertypePosition,
                    skipDeclarationSiteWildcardsIfPossible = true,
                    genericContravariantArgumentMode = contravariantArgumentMode,
                    genericInvariantArgumentMode = invariantArgumentMode)
        }

        @JvmStatic
        fun createWithConstantDeclarationSiteWildcardsMode(
                skipDeclarationSiteWildcards: Boolean,
                isForAnnotationParameter: Boolean,
                fallbackMode: TypeMappingMode? = null
        ) = TypeMappingMode(
                isForAnnotationParameter = isForAnnotationParameter,
                skipDeclarationSiteWildcards = skipDeclarationSiteWildcards,
                genericArgumentMode = fallbackMode)
    }

    fun toGenericArgumentMode(effectiveVariance: Variance): TypeMappingMode =
            when (effectiveVariance) {
                Variance.IN_VARIANCE -> genericContravariantArgumentMode ?: this
                Variance.INVARIANT -> genericInvariantArgumentMode ?: this
                else -> genericArgumentMode ?: this
            }
}
