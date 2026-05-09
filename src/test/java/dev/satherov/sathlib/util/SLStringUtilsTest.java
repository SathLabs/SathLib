package dev.satherov.sathlib.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

///
/// Unit tests for SLStringUtils class methods.
/// Tests aim to ensure all methods behave as expected according to their documentation.
///
public class SLStringUtilsTest {
    
    @Test
    public void testLower_String() {
        assertEquals("hello world", SLStringUtils.lower("HELLO WORLD"));
        assertEquals("", SLStringUtils.lower(""));
        assertEquals("1234!@#", SLStringUtils.lower("1234!@#"));
    }
    
    @Test
    public void testLower_Object() {
        assertEquals("hello world", SLStringUtils.lower((Object) "HELLO WORLD"));
        assertEquals("null", SLStringUtils.lower((Object) null));
        assertEquals("123", SLStringUtils.lower(123));
    }
    
    @Test
    public void testUpper_String() {
        assertEquals("HELLO WORLD", SLStringUtils.upper("hello world"));
        assertEquals("", SLStringUtils.upper(""));
        assertEquals("1234!@#", SLStringUtils.upper("1234!@#"));
    }
    
    @Test
    public void testUpper_Object() {
        assertEquals("HELLO WORLD", SLStringUtils.upper((Object) "hello world"));
        assertEquals("NULL", SLStringUtils.upper((Object) null));
        assertEquals("123", SLStringUtils.upper(123));
    }
    
    @Test
    public void testFormat() {
        assertEquals("Hello 123", SLStringUtils.format("Hello %d", 123));
        assertEquals("PI = 3.14", SLStringUtils.format("PI = %.2f", Math.PI));
    }
    
    @Test
    public void testToCamelCase() {
        assertEquals("helloWorld", SLStringUtils.toCamelCase("hello-world"));
        assertEquals("abcDefGhi", SLStringUtils.toCamelCase("ABC_def ghi"));
        assertEquals("", SLStringUtils.toCamelCase(""));
    }
    
    @Test
    public void testToPascalCase() {
        assertEquals("HelloWorld", SLStringUtils.toPascalCase("hello-world"));
        assertEquals("AbcDefGhi", SLStringUtils.toPascalCase("ABC_def ghi"));
        assertEquals("", SLStringUtils.toPascalCase(""));
    }
    
    @Test
    public void testToSnakeCase() {
        assertEquals("hello_world", SLStringUtils.toSnakeCase("hello-world"));
        assertEquals("abc_def_ghi", SLStringUtils.toSnakeCase("ABC def ghi"));
        assertEquals("", SLStringUtils.toSnakeCase(""));
    }
    
    @Test
    public void testToScreamingSnakeCase() {
        assertEquals("HELLO_WORLD", SLStringUtils.toScreamingSnakeCase("hello-world"));
        assertEquals("ABC_DEF_GHI", SLStringUtils.toScreamingSnakeCase("ABC def ghi"));
        assertEquals("", SLStringUtils.toScreamingSnakeCase(""));
    }
    
    @Test
    public void testToKebabCase() {
        assertEquals("hello-world", SLStringUtils.toKebabCase("Hello_World"));
        assertEquals("abc-def-ghi", SLStringUtils.toKebabCase("ABC def ghi"));
        assertEquals("", SLStringUtils.toKebabCase(""));
    }
    
    @Test
    public void testToSentenceCase() {
        assertEquals("Hello world this is a test", SLStringUtils.toSentenceCase("hello_WORLD_thisIs  aTEST"));
        assertEquals("", SLStringUtils.toSentenceCase(""));
    }
    
    @Test
    public void testToTitleCase() {
        assertEquals("Hello World This Is A Test", SLStringUtils.toTitleCase("hello_WORLD_thisIs  aTEST"));
        assertEquals("", SLStringUtils.toTitleCase(""));
    }
    
    @Test
    public void testCapitalize() {
        assertEquals("Example", SLStringUtils.capitalize("eXaMpLe"));
        assertEquals("", SLStringUtils.capitalize(""));
        assertEquals("", SLStringUtils.capitalize(null));
    }
    
    @Test
    public void testScientific() {
        assertEquals("1.2e+5", SLStringUtils.scientific(123450.0, 2));
        assertEquals("123450", SLStringUtils.scientific(123450.0, 5));
        assertEquals("123450", SLStringUtils.scientific(123450.0, 8));
        assertEquals("0", SLStringUtils.scientific(0.0, 5));
        assertEquals("∞", SLStringUtils.scientific(Double.POSITIVE_INFINITY, 5));
        assertEquals("-∞", SLStringUtils.scientific(Double.NEGATIVE_INFINITY, 5));
        assertEquals("NaN", SLStringUtils.scientific(Double.NaN, 5));
    }
    
    @Test
    public void testWords() {
        List<String> words = SLStringUtils.words("Hello_world this-Is    aTest");
        assertEquals(List.of("hello", "world", "this", "is", "a", "test"), words);
        assertTrue(SLStringUtils.words("").isEmpty());
        assertTrue(SLStringUtils.words(null).isEmpty());
    }
}