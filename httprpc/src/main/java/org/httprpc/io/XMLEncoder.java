/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.httprpc.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * XML encoder.
 */
public class XMLEncoder {
    private String rootElementName;

    /**
     * Constructs a new XML encoder.
     *
     * @param rootElementName
     * The root element name.
     */
    public XMLEncoder(String rootElementName) {
        if (rootElementName == null) {
            throw new IllegalArgumentException();
        }

        this.rootElementName = rootElementName;
    }

    /**
     * Writes a sequence of values to an output stream.
     *
     * @param values
     * The values to encode.
     *
     * @param outputStream
     * The output stream to write to.
     *
     * @throws IOException
     * If an exception occurs.
     */
    public void write(Iterable<? extends Map<String, ?>> values, OutputStream outputStream) throws IOException {
        write(values, new OutputStreamWriter(outputStream, Charset.forName("UTF-8")));
    }

    /**
     * Writes a sequence of values to a character stream.
     *
     * @param values
     * The values to encode.
     *
     * @param writer
     * The character stream to write to.
     *
     * @throws IOException
     * If an exception occurs.
     */
    public void write(Iterable<? extends Map<String, ?>> values, Writer writer) throws IOException {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

        try {
            XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(writer);

            streamWriter.writeStartDocument();

            streamWriter.writeStartElement(rootElementName);

            for (Map<String, ?> map : values) {
                streamWriter.writeStartElement("item");

                for (Map.Entry<String, ?> entry : map.entrySet()) {
                    String key = entry.getKey();

                    if (key == null) {
                        continue;
                    }

                    Object value = entry.getValue();

                    if (value == null) {
                        continue;
                    }

                    // TODO Prepend ancestor keypath

                    encode(key, value, streamWriter);
                }

                streamWriter.writeEndElement();
            }

            streamWriter.writeEndElement();

            streamWriter.writeEndDocument();
        } catch (XMLStreamException exception) {
            throw new IOException(exception);
        }

        writer.flush();
    }

    private void encode(String key, Object value, XMLStreamWriter streamWriter) throws XMLStreamException {
        if (value instanceof Enum<?>) {
            encode(key, ((Enum<?>)value).ordinal(), streamWriter);
        } else if (value instanceof Date) {
            encode(key, ((Date)value).getTime(), streamWriter);
        } else {
            streamWriter.writeAttribute(key, value.toString());
        }
    }
}
