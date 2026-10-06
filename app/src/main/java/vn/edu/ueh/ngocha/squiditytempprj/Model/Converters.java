package vn.edu.ueh.ngocha.squiditytempprj.Model;

import androidx.room.TypeConverter;

import java.util.Date;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.NotificationType;

public class Converters {
    @TypeConverter
    public static Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Date toDate(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static String fromNotificationType(NotificationType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static NotificationType toNotificationType(String value) {
        return value == null ? null : NotificationType.valueOf(value);
    }
}