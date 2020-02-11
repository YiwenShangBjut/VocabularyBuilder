package com.example.vocabularybuilder.primary_class;

public class Dict {
    private String id;
    private String Name;
    private String Comment;
    private String TimeStamp;

    public Dict(String id,String Name,String Comment,String TimeStamp){
        this.id=id;
        this.Name=Name;
        this.Comment=Comment;
        this.TimeStamp=TimeStamp;
    }
    public void setId(String id){this.id= id;}
    public void setName(String e){this.Name=e; }
    public void setComment(String p){this.Comment=p;}
    public void setTimeStamp(String p){this.TimeStamp=p;}


    public String getId(){return id;}
    public String getName(){return Name; }
    public String getComment(){return Comment;}
    public String getTimeStamp(){return TimeStamp;}


}
