package com.mauriciotogneri.betafamily;

public class Tester
{
    public String id;
    public String device_id;
    public String event_key;
    public boolean invitable;

    public boolean canBeInvited()
    {
        return (event_key == null) && invitable;
    }
}
