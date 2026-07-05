package com.camss.honeymoney.exception;

public class DuplicatedEmailException extends RuntimeException{
    public DuplicatedEmailException(String message){
        super(message);
    }
}
