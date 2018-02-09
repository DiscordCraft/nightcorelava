package io.github.phantamanta44.ncl;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;

import java.util.List;
import java.util.stream.Collectors;

public class NCLPlaylist implements AudioPlaylist {

    private final AudioPlaylist delegate;
    private final List<AudioTrack> tracks;
    private AudioTrack selected = null;

    NCLPlaylist(AudioPlaylist delegate, AudioSourceManager manager) {
        this.delegate = delegate;
        this.tracks = delegate.getTracks().stream()
                .map(t -> new NCLAudioTrack((InternalAudioTrack)t, manager))
                .collect(Collectors.toList());
        AudioTrack delSel = delegate.getSelectedTrack();
        if (delSel != null) {
            for (AudioTrack track : this.tracks) {
                if (((NCLAudioTrack)track).delegate == delSel) {
                    selected = track;
                    break;
                }
            }
        }
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public List<AudioTrack> getTracks() {
        return tracks;
    }

    @Override
    public AudioTrack getSelectedTrack() {
        return selected;
    }

    @Override
    public boolean isSearchResult() {
        return delegate.isSearchResult();
    }

}
