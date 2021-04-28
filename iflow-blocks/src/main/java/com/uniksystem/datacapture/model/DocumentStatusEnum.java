package com.uniksystem.datacapture.model;

public enum DocumentStatusEnum {
	UPLOADING, PENDING, SUCCESS, FAILED, REJECTED, REVIEWED;

    public static DocumentStatusEnum getDocumentStatusByTaskStatus(Integer status) {
        switch (status) {
        case 200:
            return DocumentStatusEnum.PENDING;
        case 303:
            return DocumentStatusEnum.SUCCESS;
        case 404:
            return DocumentStatusEnum.FAILED;
        default:
            return null;
        }
    }

    public static DocumentStatusEnum getByOrdinal(int ordinal) {
        return DocumentStatusEnum.values()[ordinal];
    }


}
