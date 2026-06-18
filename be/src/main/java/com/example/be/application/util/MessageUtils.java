package com.example.be.application.util;

public class MessageUtils {

    public static String getMessage(String key, String defaultMessage) {
        String lan = LanguageContextHolder.getLanguage();
        if ("TW".equalsIgnoreCase(lan)) {
            return getTwMessage(key, defaultMessage);
        }
        return defaultMessage;
    }

    private static String getTwMessage(String key, String defaultMessage) {
        switch (key) {
            case "SUCCESS": return "成功";
            case "ERROR": return "錯誤";
            case "FORBIDDEN": return "您沒有權限存取此資源";
            case "NOT_FOUND": return "找不到資源";
            case "UNAUTHORIZED": return "未授權或密碼不正確";
            case "VALIDATION_FAILED": return "驗證失敗";
            case "INTERNAL_ERROR": return "內部伺服器錯誤";
            case "USER_ALREADY_EXISTS": return "使用者已存在";
            case "INVALID_ROLE": return "無效的角色";
            case "FIELD_TYPE_TEXT": return "文字";
            case "FIELD_TYPE_NUMBER": return "數字";
            case "FIELD_TYPE_DATE": return "日期";
            case "FIELD_TYPE_MONEY": return "貨幣";
            case "FIELD_TYPE_PARAGRAPH": return "段落";
            case "CONTRACT_STATUS_DRAFT": return "草稿";
            case "CONTRACT_STATUS_FINALIZED": return "已完成";
            case "CONTRACT_STATUS_ARCHIVED": return "已封存";
            default:
                // Try to translate exact strings if key is not used properly
                if (defaultMessage.contains("không chính xác")) return "帳號或密碼不正確";
                if (defaultMessage.contains("đã tồn tại")) return "資料已存在";
                return defaultMessage;
        }
    }
}
