package io.github.phantamanta44.ncl;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class NCLDelegateAbsorbingLoadHandler implements AudioLoadResultHandler {

    AudioItem result = null;
    AudioResultType resultType;

    @Override
    public void trackLoaded(AudioTrack track) {
        result = track;
        resultType = AudioResultType.TRACK;
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        result = playlist;
        resultType = AudioResultType.PLAYLIST;
    }

    @Override
    public void noMatches() {
        // NO-OP
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        // NO-OP
    }

}
