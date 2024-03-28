package ua.dsns.bot.dsnsfasadtimebot.errors;

public class UserNotFoundByPhoneException extends RuntimeException{
    public UserNotFoundByPhoneException(String s){
        super("User with this phone number is not found ");
    }
}
