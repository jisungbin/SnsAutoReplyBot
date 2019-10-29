package com.sungbin.reply.bot.listener;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.sungbin.reply.bot.R;

@TargetApi(24)
public class TileServiceListener extends TileService{
    @Override
    public void onClick() {
        Tile tile = getQsTile();
        int tileState = tile.getState();
        if(tileState == Tile.STATE_INACTIVE) { //버튼 눌렀을때 활성화 되는 경우 발동
            // 보여줄 다이얼로그 생성
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.nav_header_title))
                    .setMessage("버튼 활성화")
                    .setPositiveButton("닫기", null).create();
            // 다이얼로그 호출
            showDialog(dialog);
        }
        if(tileState == Tile.STATE_ACTIVE) { //버튼 눌렀을때 비활성화 되는 경우 발동
            // 보여줄 다이얼로그 생성
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.nav_header_title))
                    .setMessage("버튼 비활성화")
                    .setPositiveButton("닫기", null).create();
            // 다이얼로그 호출
            showDialog(dialog);
        }
        if(tileState != Tile.STATE_UNAVAILABLE) {
            tile.setState(tileState == Tile.STATE_ACTIVE ? Tile.STATE_INACTIVE : Tile.STATE_ACTIVE);
            tile.updateTile();
        }
    }

    @Override
    public void onTileAdded() {
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_INACTIVE);
        tile.updateTile();
    }

}
