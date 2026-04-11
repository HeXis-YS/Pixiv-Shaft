package ceui.lisa.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import ceui.lisa.annotations.ItemHolder

class FileGeneratorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return FileGenerator(environment.codeGenerator)
    }
}

class FileGenerator(
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {
    companion object {
        private const val GENERATED_PACKAGE_SUFFIX = ".viewholdermap"
    }

    data class HolderEntry(
        val existingPackage: String,
        val itemHolder: String,
        val binding: String,
        val bindingFullname: String,
        val viewHolder: String,
    )

    private var generated = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (generated) {
            return emptyList()
        }

        val invalidSymbols = mutableListOf<KSAnnotated>()
        val holderDeclarations = mutableListOf<KSClassDeclaration>()

        resolver.getSymbolsWithAnnotation(ItemHolder::class.qualifiedName!!)
            .forEach { symbol ->
                val declaration = symbol as? KSClassDeclaration ?: return@forEach
                if (!declaration.validate()) {
                    invalidSymbols += declaration
                    return@forEach
                }
                holderDeclarations += declaration
            }

        if (holderDeclarations.isEmpty()) {
            return invalidSymbols
        }

        val holderEntries = holderDeclarations.map { declaration ->
            val annotation = declaration.annotations.first {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == ItemHolder::class.qualifiedName
            }
            val itemHolderType = annotation.arguments.first {
                it.name?.asString() == "itemHolderCls"
            }.value as KSType
            val itemHolderDeclaration = itemHolderType.declaration as KSClassDeclaration
            val itemHolderName = itemHolderDeclaration.simpleName.asString()
            val bindingDeclaration = resolveBindingDeclaration(declaration)

            HolderEntry(
                existingPackage = itemHolderDeclaration.packageName.asString() + ".",
                itemHolder = itemHolderName,
                binding = bindingDeclaration.simpleName.asString(),
                bindingFullname = bindingDeclaration.qualifiedName!!.asString(),
                viewHolder = declaration.simpleName.asString(),
            )
        }

        val buildMapEntries = holderEntries.sortedBy { it.itemHolder }.map {
            "    ${it.itemHolder}::class.java.hashCode() to ViewHolderFactory::${it.viewHolder}Builder"
        }

        val generatedPackage = holderDeclarations
            .map { it.packageName.asString() }
            .maxWithOrNull(compareBy<String>({ it.split('.').size }, { it.length }))
            .orEmpty() + GENERATED_PACKAGE_SUFFIX

        val content = StringBuilder()
        content.append("package ").append(generatedPackage)
        content.append("\n")
        content.append("\n")
        content.append("import android.view.LayoutInflater\n")
        content.append("import android.view.ViewGroup\n")
        content.append("import android.view.View\n")
        content.append("import androidx.viewbinding.ViewBinding\n")
        content.append("import ceui.refactor.ListItemHolder\n")
        content.append("import ceui.refactor.ListItemViewHolder\n")
        holderEntries.sortedBy { it.viewHolder }.forEach {
            content.append("import ${ it.existingPackage }${it.viewHolder}\n")
        }
        holderEntries.sortedBy { it.itemHolder }.forEach {
            content.append("import ${ it.existingPackage }${it.itemHolder}\n")
        }
        content.append(holderEntries.filter { it.binding.endsWith("Binding") }.map { "import ${it.bindingFullname}" }.distinct().joinToString("\n"))
        content.append("\n")
        content.append("\n")
        content.append("object ViewHolderFactory {\n")
        holderEntries.sortedBy { it.viewHolder }.forEach {
            content.append("\n")
            content.append("    private fun ${it.viewHolder}Builder(parent: ViewGroup): ListItemViewHolder<out ViewBinding, out ListItemHolder> {")
            content.append("\n")
            content.append("        val binding = ${it.binding}.inflate(\n" +
                    "            LayoutInflater.from(parent.context),\n" +
                    "            parent,\n" +
                    "            false\n" +
                    "        )")
            content.append("\n")
            content.append("        return ${it.viewHolder}(binding)")
            content.append("\n")
            content.append("    }")
            content.append("\n")
        }

        content.append("\n")
        content.append("    val VIEW_HOLDER_MAP = mapOf(\n    " + buildMapEntries.joinToString(",\n    ") + "     \n    )\n\n\n")


        content.append("}")
        content.append("\n")
        content.append("\n")


        val sourceFiles = holderDeclarations.mapNotNull(KSClassDeclaration::containingFile).distinct()
        codeGenerator.createNewFile(
            Dependencies(true, *sourceFiles.toTypedArray()),
            generatedPackage,
            "ViewHolderMap",
            "kt"
        ).bufferedWriter().use { writer ->
            writer.write(content.toString())
        }

        generated = true
        return invalidSymbols
    }

    private fun resolveBindingDeclaration(declaration: KSClassDeclaration): KSClassDeclaration {
        val constructor = declaration.primaryConstructor ?: declaration.declarations
            .filterIsInstance<KSFunctionDeclaration>()
            .firstOrNull { it.simpleName.asString() == "<init>" }
        val firstParameter = constructor?.parameters?.firstOrNull()
            ?: error("No constructor parameter found for ${declaration.qualifiedName?.asString()}")
        return firstParameter.type.resolve().declaration as? KSClassDeclaration
            ?: error("Binding parameter is not a class declaration for ${declaration.qualifiedName?.asString()}")
    }
}
