package com.example.vocabularybuilder.primary_class;


public class Word {
    private String id;
    private String English;
    private String Phonetic;
    private String Property;
    private String Chinese;
    private String Meaning;
    private String photoPath="";
    private String timeStamp;
    private String Location;

    public Word(){
        Location="";
    }
    public void setId(String id){this.id= id;}
    public void setEnglish(String e){this.English=e; }
    public void setPhonetic(String p){this.Phonetic=p;}
    public void setProperty(String p){this.Property=p;}
    public void setChinese(String c){this.Chinese=c; }
    public void setMeaning(String m){this.Meaning=m; }
    public void setPhotoPath(String m){this.photoPath=m; }
    public void setTimeStamp(String m){this.timeStamp=m; }
    public void setLocation(String m){this.Location=m; }

    public String getId(){return id;}
    public String getEnglish(){return English; }
    public String getPhonetic(){return Phonetic;}
    public String getProperty(){return Property;}
    public String getChinese(){return Chinese; }
    public String getMeaning(){return Meaning; }
    public String getPhotoPath(){return photoPath; }
    public String getTimeStamp(){return timeStamp; }
    public String getLocation(){return Location; }

}

