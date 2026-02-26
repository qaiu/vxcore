package cn.qaiu.db.dsl.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class EnhancedTypeMapperTest {

  @AfterEach
  void cleanup() {
    EnhancedTypeMapper.removeTypeConverter(Double.class);
  }

  @Nested
  class ConvertToTypeTest {

    @Test
    void nullValue_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType(null, String.class)).isNull();
    }

    @Test
    void sameType_returnsCast() {
      String value = "hello";
      assertThat(EnhancedTypeMapper.convertToType(value, String.class)).isEqualTo("hello");
    }

    @Test
    void stringFromNumber() {
      assertThat(EnhancedTypeMapper.convertToType(42, String.class)).isEqualTo("42");
    }

    @Test
    void longFromNumber() {
      assertThat(EnhancedTypeMapper.convertToType(42, Long.class)).isEqualTo(42L);
    }

    @Test
    void longFromString() {
      assertThat(EnhancedTypeMapper.convertToType("12345", Long.class)).isEqualTo(12345L);
    }

    @Test
    void longFromInvalidString_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType("abc", Long.class)).isNull();
    }

    @Test
    void longFromUnsupportedType_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType(new Object(), Long.class)).isNull();
    }

    @Test
    void integerFromNumber() {
      assertThat(EnhancedTypeMapper.convertToType(42L, Integer.class)).isEqualTo(42);
    }

    @Test
    void integerFromString() {
      assertThat(EnhancedTypeMapper.convertToType("42", Integer.class)).isEqualTo(42);
    }

    @Test
    void integerFromInvalidString_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType("xyz", Integer.class)).isNull();
    }

    @Test
    void integerFromUnsupportedType_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType(new Object(), Integer.class)).isNull();
    }

    @Test
    void bigDecimalFromBigDecimal() {
      BigDecimal val = new BigDecimal("3.14");
      assertThat(EnhancedTypeMapper.convertToType(val, BigDecimal.class)).isEqualTo(val);
    }

    @Test
    void bigDecimalFromNumber() {
      assertThat(EnhancedTypeMapper.convertToType(3.14, BigDecimal.class))
          .isEqualByComparingTo("3.14");
    }

    @Test
    void bigDecimalFromString() {
      assertThat(EnhancedTypeMapper.convertToType("99.99", BigDecimal.class))
          .isEqualByComparingTo("99.99");
    }

    @Test
    void bigDecimalFromInvalidString_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType("not_a_number", BigDecimal.class)).isNull();
    }

    @Test
    void bigDecimalFromUnsupportedType_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType(new Object(), BigDecimal.class)).isNull();
    }

    @Test
    void booleanFromBoolean() {
      assertThat(EnhancedTypeMapper.convertToType(true, Boolean.class)).isTrue();
    }

    @Test
    void booleanFromStringTrue() {
      assertThat(EnhancedTypeMapper.convertToType("true", Boolean.class)).isTrue();
      assertThat(EnhancedTypeMapper.convertToType("1", Boolean.class)).isTrue();
      assertThat(EnhancedTypeMapper.convertToType("yes", Boolean.class)).isTrue();
    }

    @Test
    void booleanFromStringFalse() {
      assertThat(EnhancedTypeMapper.convertToType("false", Boolean.class)).isFalse();
      assertThat(EnhancedTypeMapper.convertToType("0", Boolean.class)).isFalse();
      assertThat(EnhancedTypeMapper.convertToType("no", Boolean.class)).isFalse();
    }

    @Test
    void booleanFromInvalidString_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType("maybe", Boolean.class)).isNull();
    }

    @Test
    void booleanFromNumber() {
      assertThat(EnhancedTypeMapper.convertToType(1, Boolean.class)).isTrue();
      assertThat(EnhancedTypeMapper.convertToType(0, Boolean.class)).isFalse();
    }

    @Test
    void booleanFromUnsupportedType_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType(new Object(), Boolean.class)).isNull();
    }

    @Test
    void localDateTimeFromLocalDateTime() {
      LocalDateTime now = LocalDateTime.now();
      assertThat(EnhancedTypeMapper.convertToType(now, LocalDateTime.class)).isEqualTo(now);
    }

    @Test
    void localDateTimeFromTimestamp() {
      LocalDateTime expected = LocalDateTime.of(2025, 1, 15, 10, 30, 0);
      Timestamp ts = Timestamp.valueOf(expected);
      assertThat(EnhancedTypeMapper.convertToType(ts, LocalDateTime.class)).isEqualTo(expected);
    }

    @Test
    void localDateTimeFromIsoString() {
      String dateStr = "2025-01-15T10:30:00";
      LocalDateTime result = EnhancedTypeMapper.convertToType(dateStr, LocalDateTime.class);
      assertThat(result).isEqualTo(LocalDateTime.of(2025, 1, 15, 10, 30, 0));
    }

    @Test
    void localDateTimeFromSqlString() {
      String dateStr = "2025-01-15 10:30:00";
      LocalDateTime result = EnhancedTypeMapper.convertToType(dateStr, LocalDateTime.class);
      assertThat(result).isEqualTo(LocalDateTime.of(2025, 1, 15, 10, 30, 0));
    }

    @Test
    void localDateTimeFromMicrosString() {
      String dateStr = "2025-01-15 10:30:00.123456";
      LocalDateTime result = EnhancedTypeMapper.convertToType(dateStr, LocalDateTime.class);
      assertThat(result).isNotNull();
    }

    @Test
    void localDateTimeFromIsoMicrosString() {
      String dateStr = "2025-01-15T10:30:00.123456";
      LocalDateTime result = EnhancedTypeMapper.convertToType(dateStr, LocalDateTime.class);
      assertThat(result).isNotNull();
    }

    @Test
    void localDateTimeFromPartialMicrosString() {
      String dateStr = "2025-01-15 10:30:00.12";
      LocalDateTime result = EnhancedTypeMapper.convertToType(dateStr, LocalDateTime.class);
      assertThat(result).isNotNull();
    }

    @Test
    void localDateTimeFromLongMicrosString() {
      String dateStr = "2025-01-15 10:30:00.12345678";
      LocalDateTime result = EnhancedTypeMapper.convertToType(dateStr, LocalDateTime.class);
      assertThat(result).isNotNull();
    }

    @Test
    void localDateTimeFromInvalidString_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType("not_a_date", LocalDateTime.class)).isNull();
    }

    @Test
    void localDateTimeFromEmpty_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType("", LocalDateTime.class)).isNull();
    }

    @Test
    void localDateTimeFromUnsupportedType_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType(new Object(), LocalDateTime.class)).isNull();
    }

    @Test
    void localDateTimeFromNull_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType(null, LocalDateTime.class)).isNull();
    }

    @Test
    void enumConversion() {
      assertThat(EnhancedTypeMapper.convertToType("VALUE_A", TestEnum.class))
          .isEqualTo(TestEnum.VALUE_A);
    }

    @Test
    void enumConversion_invalidValue_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType("INVALID", TestEnum.class)).isNull();
    }

    @Test
    void enumConversion_nonString_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType(42, TestEnum.class)).isNull();
    }

    @Test
    void incompatibleCast_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType("text", Integer.TYPE)).isNull();
    }

    @Test
    void longFromNull_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType(null, Long.class)).isNull();
    }

    @Test
    void integerFromNull_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType(null, Integer.class)).isNull();
    }

    @Test
    void bigDecimalFromNull_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType(null, BigDecimal.class)).isNull();
    }

    @Test
    void booleanFromNull_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType(null, Boolean.class)).isNull();
    }

    @Test
    void stringFromNull_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToType(null, String.class)).isNull();
    }

    @Test
    void localDateTimeFromSqlMicrosString() {
      String dateStr = "2025-01-15 10:30:00.000123";
      LocalDateTime result = EnhancedTypeMapper.convertToType(dateStr, LocalDateTime.class);
      assertThat(result).isNotNull();
    }

    @Test
    void localDateTimeFromIsoWithTNormalized() {
      String dateStr = "2025-01-15T10:30:00";
      LocalDateTime result = EnhancedTypeMapper.convertToType(dateStr, LocalDateTime.class);
      assertThat(result).isNotNull();
      assertThat(result.getHour()).isEqualTo(10);
    }

    @Test
    void booleanFromStringUpperCase() {
      assertThat(EnhancedTypeMapper.convertToType("TRUE", Boolean.class)).isTrue();
      assertThat(EnhancedTypeMapper.convertToType("FALSE", Boolean.class)).isFalse();
      assertThat(EnhancedTypeMapper.convertToType("YES", Boolean.class)).isTrue();
      assertThat(EnhancedTypeMapper.convertToType("NO", Boolean.class)).isFalse();
    }

    @Test
    void booleanFromZeroNumber() {
      assertThat(EnhancedTypeMapper.convertToType(0L, Boolean.class)).isFalse();
      assertThat(EnhancedTypeMapper.convertToType(42L, Boolean.class)).isTrue();
    }
  }

  @Nested
  class ConvertToDatabaseValueTest {

    @Test
    void nullValue_returnsNull() {
      assertThat(EnhancedTypeMapper.convertToDatabaseValue(null)).isNull();
    }

    @Test
    void localDateTime_convertsToTimestamp() {
      LocalDateTime dt = LocalDateTime.of(2025, 6, 15, 12, 0, 0);
      Object result = EnhancedTypeMapper.convertToDatabaseValue(dt);
      assertThat(result).isInstanceOf(Timestamp.class);
    }

    @Test
    void bigDecimal_returnsAsIs() {
      BigDecimal bd = new BigDecimal("99.99");
      assertThat(EnhancedTypeMapper.convertToDatabaseValue(bd)).isSameAs(bd);
    }

    @Test
    void dateTimeString_convertsToTimestamp() {
      Object result = EnhancedTypeMapper.convertToDatabaseValue("2025-01-15 10:30:00");
      assertThat(result).isInstanceOf(Timestamp.class);
    }

    @Test
    void numericString_convertsToBigDecimal() {
      Object result = EnhancedTypeMapper.convertToDatabaseValue("42.5");
      assertThat(result).isInstanceOf(BigDecimal.class);
    }

    @Test
    void plainString_returnsAsIs() {
      assertThat(EnhancedTypeMapper.convertToDatabaseValue("hello")).isEqualTo("hello");
    }

    @Test
    void otherType_returnsAsIs() {
      Integer val = 42;
      assertThat(EnhancedTypeMapper.convertToDatabaseValue(val)).isEqualTo(42);
    }

    @Test
    void negativeNumericString() {
      Object result = EnhancedTypeMapper.convertToDatabaseValue("-123");
      assertThat(result).isInstanceOf(BigDecimal.class);
    }

    @Test
    void nonNumericNonDateString() {
      Object result = EnhancedTypeMapper.convertToDatabaseValue("abc");
      assertThat(result).isEqualTo("abc");
    }

    @Test
    void emptyString() {
      Object result = EnhancedTypeMapper.convertToDatabaseValue("");
      assertThat(result).isEqualTo("");
    }
  }

  @Nested
  class CustomConverterTest {

    @Test
    void addAndUseCustomConverter() {
      EnhancedTypeMapper.addTypeConverter(Double.class, obj -> {
        if (obj instanceof String) return Double.parseDouble((String) obj);
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        return null;
      });
      assertThat(EnhancedTypeMapper.convertToType("3.14", Double.class)).isEqualTo(3.14);
    }

    @Test
    void removeCustomConverter() {
      EnhancedTypeMapper.addTypeConverter(Double.class, obj -> 0.0);
      EnhancedTypeMapper.removeTypeConverter(Double.class);
      Map<Class<?>, Function<Object, Object>> converters = EnhancedTypeMapper.getSupportedConverters();
      assertThat(converters).doesNotContainKey(Double.class);
    }

    @Test
    void removeStringConverter_restoresDefault() {
      EnhancedTypeMapper.removeTypeConverter(String.class);
      assertThat(EnhancedTypeMapper.convertToType(42, String.class)).isEqualTo("42");
    }

    @Test
    void getSupportedConverters_returnsNonEmpty() {
      Map<Class<?>, Function<Object, Object>> converters = EnhancedTypeMapper.getSupportedConverters();
      assertThat(converters).isNotEmpty();
      assertThat(converters).containsKey(String.class);
      assertThat(converters).containsKey(Long.class);
      assertThat(converters).containsKey(Integer.class);
      assertThat(converters).containsKey(BigDecimal.class);
      assertThat(converters).containsKey(Boolean.class);
      assertThat(converters).containsKey(LocalDateTime.class);
    }
  }

  enum TestEnum {
    VALUE_A, VALUE_B
  }
}
