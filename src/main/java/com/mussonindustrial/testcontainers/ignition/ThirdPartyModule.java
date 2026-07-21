package com.mussonindustrial.testcontainers.ignition;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Describes a third-party Ignition module archive.
 *
 * @param archive module archive
 * @param identifier module identifier
 * @param gatewayDependencies gateway-scoped module dependencies
 */
public record ThirdPartyModule(Path archive, String identifier, Set<String> gatewayDependencies) {

    private static final String DESCRIPTOR_PATH = "module.xml";
    private static final String MODULE_ELEMENT = "module";
    private static final String IDENTIFIER_ELEMENT = "id";
    private static final String DEPENDENCY_ELEMENT = "depends";
    private static final String GATEWAY_SCOPE = "G";

    /**
     * Validates and normalizes the module descriptor.
     *
     * @param archive module archive
     * @param identifier module identifier
     * @param gatewayDependencies gateway-scoped module dependencies
     */
    public ThirdPartyModule {
        archive = normalizePath(archive);
        identifier = requireNonBlank(identifier, "identifier");
        gatewayDependencies = immutableDependencies(gatewayDependencies);
    }

    /**
     * Reads a third-party module archive.
     *
     * @param archive module archive
     * @return parsed module descriptor
     * @throws FileNotFoundException if the module archive does not exist
     * @throws IllegalArgumentException if the archive or descriptor is invalid
     */
    public static ThirdPartyModule read(Path archive) throws FileNotFoundException {
        Path normalizedArchive = requireArchive(archive);

        try (ZipFile moduleFile = new ZipFile(normalizedArchive.toFile())) {
            ZipEntry descriptor = moduleFile.getEntry(DESCRIPTOR_PATH);

            if (descriptor == null) {
                throw new IllegalArgumentException(
                        "Module archive does not contain %s: %s".formatted(DESCRIPTOR_PATH, normalizedArchive));
            }

            try (InputStream input = moduleFile.getInputStream(descriptor)) {
                return readDescriptor(normalizedArchive, input);
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to read module archive: " + normalizedArchive, exception);
        }
    }

    /**
     * Reads a module descriptor.
     */
    private static ThirdPartyModule readDescriptor(Path archive, InputStream input) {
        try {
            NodeList modules = createDocumentBuilderFactory()
                    .newDocumentBuilder()
                    .parse(input)
                    .getElementsByTagName(MODULE_ELEMENT);

            if (modules.getLength() == 0) {
                throw new IllegalArgumentException(
                        "%s does not contain a <%s> element: %s".formatted(DESCRIPTOR_PATH, MODULE_ELEMENT, archive));
            }

            Element module = (Element) modules.item(0);

            return new ThirdPartyModule(
                    archive, requiredText(module, IDENTIFIER_ELEMENT, archive), gatewayDependencies(module));
        } catch (ParserConfigurationException | SAXException | IOException exception) {
            throw new IllegalArgumentException("Unable to read module descriptor: " + archive, exception);
        }
    }

    /**
     * Creates a securely configured XML parser.
     */
    private static DocumentBuilderFactory createDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();

        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);

        return factory;
    }

    /**
     * Reads Gateway-scoped dependencies.
     */
    private static Set<String> gatewayDependencies(Element module) {
        NodeList elements = module.getElementsByTagName(DEPENDENCY_ELEMENT);

        LinkedHashSet<String> dependencies = new LinkedHashSet<>();

        for (int index = 0; index < elements.getLength(); index++) {
            Element dependency = (Element) elements.item(index);

            if (!GATEWAY_SCOPE.equals(dependency.getAttribute("scope"))) {
                continue;
            }

            String identifier = dependency.getTextContent().strip();

            if (!identifier.isEmpty()) {
                dependencies.add(identifier);
            }
        }

        return dependencies;
    }

    /**
     * Returns required element text.
     */
    private static String requiredText(Element parent, String elementName, Path archive) {
        NodeList elements = parent.getElementsByTagName(elementName);

        if (elements.getLength() == 0) {
            throw new IllegalArgumentException(
                    "%s does not contain <%s>: %s".formatted(DESCRIPTOR_PATH, elementName, archive));
        }

        String value = elements.item(0).getTextContent().strip();

        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "%s contains an empty <%s>: %s".formatted(DESCRIPTOR_PATH, elementName, archive));
        }

        return value;
    }

    /**
     * Returns normalized immutable dependencies.
     */
    private static Set<String> immutableDependencies(Set<String> dependencies) {
        Objects.requireNonNull(dependencies, "gatewayDependencies");

        LinkedHashSet<String> normalized = new LinkedHashSet<>();

        for (String dependency : dependencies) {
            normalized.add(requireNonBlank(dependency, "gateway dependency"));
        }

        return Collections.unmodifiableSet(normalized);
    }

    /**
     * Requires an existing module archive.
     */
    private static Path requireArchive(Path archive) throws FileNotFoundException {
        Path normalized = normalizePath(archive);

        if (!Files.isRegularFile(normalized)) {
            throw new FileNotFoundException(
                    "Module archive does not exist " + "or is not a regular file: " + normalized);
        }

        return normalized;
    }

    /**
     * Normalizes an archive path.
     */
    private static Path normalizePath(Path archive) {
        return Objects.requireNonNull(archive, "archive").toAbsolutePath().normalize();
    }

    /**
     * Validates a required string.
     */
    private static String requireNonBlank(String value, String name) {
        String normalized = Objects.requireNonNull(value, name).strip();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }

        if (normalized.indexOf('\0') >= 0) {
            throw new IllegalArgumentException(name + " cannot contain a null character");
        }

        return normalized;
    }
}
