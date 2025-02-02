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

package org.httprpc.test;

import org.httprpc.Content;
import org.httprpc.Description;
import org.httprpc.RequestMethod;
import org.httprpc.ResourcePath;
import org.httprpc.WebService;
import org.httprpc.beans.BeanAdapter;
import org.httprpc.beans.Key;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.httprpc.util.Collections.entry;
import static org.httprpc.util.Collections.listOf;
import static org.httprpc.util.Collections.mapOf;

@WebServlet(urlPatterns = {"/test/*"}, loadOnStartup = 1)
@MultipartConfig
public class TestService extends WebService {
    public interface Response {
        String getString();
        List<String> getStrings();
        int getNumber();
        boolean getFlag();
        DayOfWeek getDayOfWeek();
        Date getDate();
        Instant getInstant();
        LocalDate getLocalDate();
        LocalTime getLocalTime();
        LocalDateTime getLocalDateTime();
        Duration getDuration();
        Period getPeriod();
        @Key("uuid")
        UUID getUUID();
        List<AttachmentInfo> getAttachmentInfo();
    }

    public interface AttachmentInfo {
        int getBytes();
        int getChecksum();
        URL getAttachment();
    }

    public interface A {
        int getA();
    }

    public interface B {
        @Description("B's version of B")
        double getB();
    }

    public interface C extends A, B {
        @Override
        @Description("C's version of B")
        double getB();
        String getC();
    }

    public static class D {
        public int getD() {
            return 0;
        }
    }

    public static class E extends D {
        public double getE() {
            return 0;
        }
    }

    public static class TestList extends ArrayList<Integer> {
    }

    public static class TestMap extends HashMap<String, Double> {
    }

    public interface Body {
        String getString();
        List<String> getStrings();
        int getNumber();
        boolean getFlag();
    }

    @RequestMethod("GET")
    public Map<String, Object> testGet(String string, List<String> strings, int number, boolean flag, DayOfWeek dayOfWeek,
        Date date, Instant instant, LocalDate localDate, LocalTime localTime, LocalDateTime localDateTime,
        Duration duration, Period period,
        UUID uuid) {
        return mapOf(
            entry("string", string),
            entry("strings", strings),
            entry("number", number),
            entry("flag", flag),
            entry("dayOfWeek", dayOfWeek),
            entry("date", date),
            entry("instant", instant),
            entry("localDate", localDate),
            entry("localTime", localTime),
            entry("localDateTime", localDateTime),
            entry("duration", duration),
            entry("period", period),
            entry("uuid", uuid)
        );
    }

    @RequestMethod("GET")
    @ResourcePath("a/?:a/b/?/c/?:c/d/?")
    public Map<String, Object> testGetKeys() {
        return mapOf(
            entry("list", listOf(getKey(0), getKey(1), getKey(2), getKey(3))),
            entry("map", mapOf(
                entry("a", getKey("a")),
                entry("b", getKey("b")),
                entry("c", getKey("c")),
                entry("d", getKey("d"))
            ))
        );
    }

    @RequestMethod("GET")
    @ResourcePath("fibonacci")
    public Iterable<BigInteger> testGetFibonacci(int count) {
        return () -> new Iterator<BigInteger>() {
            private int i = 0;

            private BigInteger a = BigInteger.valueOf(0);
            private BigInteger b = BigInteger.valueOf(1);

            @Override
            public boolean hasNext() {
                return i < count;
            }

            @Override
            public BigInteger next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                BigInteger next;
                if (i == 0) {
                    next = a;
                } else {
                    if (i > 1) {
                        BigInteger c = a.add(b);

                        a = b;
                        b = c;
                    }

                    next = b;
                }

                i++;

                return next;
            }
        };
    }

    @RequestMethod("GET")
    @ResourcePath("c")
    public C testGetC() {
        return null;
    }

    @RequestMethod("GET")
    @ResourcePath("e")
    public E testGetE() {
        return null;
    }

    @RequestMethod("GET")
    @ResourcePath("list")
    public TestList testGetList() {
        return new TestList();
    }

    @RequestMethod("GET")
    @ResourcePath("map")
    public TestMap testGetMap() {
        return new TestMap();
    }

    @RequestMethod("GET")
    @ResourcePath("generic")
    public Iterable<List<Map<String, Double>>> testGetGeneric() {
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();

        writer.append("Unsupported operation.");
        writer.flush();
    }

    @RequestMethod("POST")
    public Response testPost(String string, List<String> strings, int number, boolean flag, DayOfWeek dayOfWeek,
        Date date, Instant instant, LocalDate localDate, LocalTime localTime, LocalDateTime localDateTime,
        Duration duration, Period period,
        UUID uuid, List<URL> attachments) throws IOException {
        List<Map<String, ?>> attachmentInfo = new LinkedList<>();

        for (URL attachment : attachments) {
            long bytes = 0;
            long checksum = 0;

            try (InputStream inputStream = attachment.openStream()) {
                int b;
                while ((b = inputStream.read()) != -1) {
                    bytes++;
                    checksum += b;
                }
            }

            attachmentInfo.add(mapOf(
                entry("bytes", bytes),
                entry("checksum", checksum),
                entry("attachment", attachment)
            ));
        }

        return BeanAdapter.coerce(mapOf(
            entry("string", string),
            entry("strings", strings),
            entry("number", number),
            entry("flag", flag),
            entry("dayOfWeek", dayOfWeek),
            entry("date", date),
            entry("instant", instant),
            entry("localDate", localDate),
            entry("localTime", localTime),
            entry("localDateTime", localDateTime),
            entry("duration", duration),
            entry("period", period),
            entry("uuid", uuid),
            entry("attachmentInfo", attachmentInfo)
        ), Response.class);
    }

    @RequestMethod("POST")
    @Content(Body.class)
    public Body testPost(int id) {
        return getBody();
    }

    @RequestMethod("POST")
    public void testPost(String name) throws IOException {
        echo();
    }

    @RequestMethod("PUT")
    public void testPut(int id) throws IOException {
        echo();
    }

    private void echo() throws IOException {
        InputStream inputStream = getRequest().getInputStream();
        OutputStream outputStream = getResponse().getOutputStream();

        int b;
        while ((b = inputStream.read()) != -1) {
            outputStream.write(b);
        }

        outputStream.flush();
    }

    @RequestMethod("DELETE")
    public void testDelete(int id) {
        // No-op
    }

    @RequestMethod("GET")
    @ResourcePath("headers")
    public Map<String, String> testHeaders() {
        HttpServletRequest request = getRequest();

        return mapOf(
            entry("X-Header-A", request.getHeader("X-Header-A")),
            entry("X-Header-B", request.getHeader("X-Header-B"))
        );
    }

    @RequestMethod("GET")
    @ResourcePath("deprecated")
    @Deprecated
    public void testDeprecated() {
        // No-op
    }

    @RequestMethod("GET")
    @ResourcePath("unauthorized")
    public void testUnauthorized() {
        // No-op
    }

    @RequestMethod("GET")
    @ResourcePath("error")
    public void testError() throws Exception {
        throw new Exception("Sample error message.");
    }

    @RequestMethod("GET")
    public int testTimeout(int value, int delay) throws InterruptedException {
        Thread.sleep(delay);

        return value;
    }

    @RequestMethod("GET")
    @ResourcePath("math/sum")
    public double getSum(double a, double b) {
        return getInstance(MathService.class).getSum(a, b);
    }

    @RequestMethod("GET")
    @ResourcePath("math/sum")
    public double getSum(List<Double> values) {
        return getInstance(MathService.class).getSum(values);
    }

    @Override
    protected boolean isAuthorized(HttpServletRequest request, Method method) {
        String pathInfo = request.getPathInfo();

        return (pathInfo == null || !pathInfo.endsWith("unauthorized"));
    }
}
