package com.yunisrajab.curator;

import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class User {

    public String email, password, uid;
    HashMap<String ,    Boolean>    votes;
    Set<String> favs;

    public User(String email, String password,  String  uid) {
        this.email = email;
        this.password = password;
        this.uid = uid;
    }

    public void setVotes    (HashMap<String ,    Boolean>    votes)  {
        this.votes  =   votes;
    }

    public HashMap<String, Boolean> getVotes() {
        if (votes   ==  null)   return new HashMap<String, Boolean>();
        return votes;
    }

    public void setVotes    (Set<String> strings)  {
        HashMap<String, Boolean>    map =   new HashMap<>();
        if (strings !=  null)   {
            String[] array = strings.toArray(new String[strings.size()]);
            for (int   i   =   0;    i<array.length;    i++)    {
                String  key =   array[i].substring(0,   array[i].indexOf("="));
                String  value   =   array[i].substring(array[i].lastIndexOf("=") + 1);
                map.put(key,  Boolean.valueOf(value));
            }
        }
        votes  =   map;
    }

    public Set<String> getVotesStrings() {
        if (votes !=null) {
            String[] keys = new String[votes.size()];
            int k   =   0;
            for ( String key : votes.keySet() ) {
                keys[k] =   key;
                k++;
            }

            Set<String> strings =   new HashSet<>();
            for (int    i=0;    i<keys.length; i++)    {
                strings.add(keys[i]+"="+String.valueOf(votes.get(keys[i])));
            }
            return strings;
        }   else return new HashSet<String>();
    }

    public void setFavs(Set<String> favs) {
        this.favs = favs;
    }

    public Set<String> getFavs() {
        if (favs    ==  null)   return new HashSet<>();
        else return favs;
    }
}
