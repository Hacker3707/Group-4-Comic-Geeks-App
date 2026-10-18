package vn.edu.ueh.ngocha.squiditytempprj.Model;

import androidx.room.TypeConverter;

import java.util.Date;
import java.util.List;
import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.NotificationType;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.StampType;

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

    @TypeConverter
    public static String fromStampType(StampType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static StampType toStampType(String value) {
        return value == null ? null : StampType.valueOf(value);
    }
    @TypeConverter
    public static List<String> fromString(String value) {
        if (value == null) {
            return null;
        }
        Type listType = new TypeToken<List<String>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromList(List<String> list) {
        if (list == null) {
            return null;
        }
        return new Gson().toJson(list);
    }
}