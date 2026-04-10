package ceui.refactor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType

object ViewBindingCompat {

    @Suppress("UNCHECKED_CAST")
    fun <T : ViewBinding> inflate(
        ownerClass: Class<*>,
        baseClass: Class<*>,
        typeArgumentIndex: Int,
        inflater: LayoutInflater,
        parent: ViewGroup? = null,
        attachToParent: Boolean = false,
    ): T {
        val bindingClass = resolveViewBindingClass(ownerClass, baseClass, typeArgumentIndex)
        return inflate(bindingClass, inflater, parent, attachToParent) as T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : ViewBinding> bind(
        ownerClass: Class<*>,
        baseClass: Class<*>,
        typeArgumentIndex: Int,
        view: View,
    ): T {
        val bindingClass = resolveViewBindingClass(ownerClass, baseClass, typeArgumentIndex)
        return bindingClass.getMethod("bind", View::class.java).invoke(null, view) as T
    }

    @Suppress("UNCHECKED_CAST")
    private fun inflate(
        bindingClass: Class<out ViewBinding>,
        inflater: LayoutInflater,
        parent: ViewGroup?,
        attachToParent: Boolean,
    ): ViewBinding {
        return try {
            val inflateMethod = bindingClass.getMethod(
                "inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Boolean::class.javaPrimitiveType,
            )
            inflateMethod.invoke(null, inflater, parent, attachToParent) as ViewBinding
        } catch (_: NoSuchMethodException) {
            val inflateMethod = bindingClass.getMethod("inflate", LayoutInflater::class.java)
            inflateMethod.invoke(null, inflater) as ViewBinding
        }
    }

    private fun resolveViewBindingClass(
        ownerClass: Class<*>,
        baseClass: Class<*>,
        typeArgumentIndex: Int,
    ): Class<out ViewBinding> {
        val bindingClass = resolveTypeArgument(ownerClass, baseClass, typeArgumentIndex)
            ?: error("Cannot resolve ViewBinding for ${ownerClass.name} from ${baseClass.name}")
        require(ViewBinding::class.java.isAssignableFrom(bindingClass)) {
            "Resolved type is not a ViewBinding: ${bindingClass.name}"
        }
        @Suppress("UNCHECKED_CAST")
        return bindingClass as Class<out ViewBinding>
    }

    private fun resolveTypeArgument(
        ownerClass: Class<*>,
        baseClass: Class<*>,
        typeArgumentIndex: Int,
    ): Class<*>? {
        val resolvedTypes = LinkedHashMap<TypeVariable<*>, Type>()
        var currentClass: Class<*> = ownerClass

        while (currentClass != baseClass) {
            val genericSuperclass = currentClass.genericSuperclass ?: return null
            when (genericSuperclass) {
                is ParameterizedType -> {
                    val rawType = genericSuperclass.rawType as? Class<*> ?: return null
                    rawType.typeParameters.zip(genericSuperclass.actualTypeArguments).forEach { (variable, actualType) ->
                        resolvedTypes[variable] = resolveType(actualType, resolvedTypes)
                    }
                    currentClass = rawType
                }

                is Class<*> -> currentClass = genericSuperclass
                else -> return null
            }
        }

        return resolveToClass(currentClass.typeParameters[typeArgumentIndex], resolvedTypes)
    }

    private fun resolveType(type: Type, resolvedTypes: Map<TypeVariable<*>, Type>): Type {
        return when (type) {
            is TypeVariable<*> -> resolvedTypes[type]?.let { resolveType(it, resolvedTypes) } ?: type
            else -> type
        }
    }

    private fun resolveToClass(type: Type, resolvedTypes: Map<TypeVariable<*>, Type>): Class<*>? {
        val resolvedType = resolveType(type, resolvedTypes)
        return when (resolvedType) {
            is Class<*> -> resolvedType
            is ParameterizedType -> resolvedType.rawType as? Class<*>
            is WildcardType -> resolvedType.upperBounds.firstNotNullOfOrNull {
                resolveToClass(it, resolvedTypes)
            }

            else -> null
        }
    }
}
