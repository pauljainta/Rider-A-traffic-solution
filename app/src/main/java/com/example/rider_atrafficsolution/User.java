package com.example.rider_atrafficsolution;

public interface User {

}

class Passenger implements User{

    String name,email,phone_number;

    public Passenger(String name, String email, String phone_number) {
        this.name = name;
        this.email = email;
        this.phone_number = phone_number;
    }
}


