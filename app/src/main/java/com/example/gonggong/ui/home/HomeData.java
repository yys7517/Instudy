package com.example.gonggong.ui.home;

import android.net.Uri;

public class HomeData {

    private String userid; //게시물 작성자 ID
    private String code; //게시물 기본키
    private String nickname; //사용자 이름
    private String contents; //내용
    private String date; //날짜

    private String imgPost; // 게시글 사진 경로
    private String imgProfile; // 프로필 사진 경로

    //게시물 작성자 ID
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    //게시물 기본키
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    //사용자 이름
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    //내용
    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    //날짜
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getImgPost() {
        return imgPost;
    }

    public void setImgPost(String imgPost) {
        this.imgPost = imgPost;
    }
}
































