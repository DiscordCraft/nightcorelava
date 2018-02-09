package io.github.phantamanta44.ncl;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class NCLDelegateSourceManager implements AudioSourceManager {

    private final List<AudioSourceManager> sourceManagers;
    private final Method mCheckSourcesForItem;

    @SuppressWarnings("unchecked")
    public NCLDelegateSourceManager(DefaultAudioPlayerManager manager) {
        try {
            Field f = manager.getClass().getDeclaredField("sourceManagers");
            f.setAccessible(true);
            this.sourceManagers = (List<AudioSourceManager>)f.get(manager);
            mCheckSourcesForItem = manager.getClass().getDeclaredMethod("checkSourcesForItem",
                    AudioReference.class, AudioLoadResultHandler.class, boolean[].class);
            mCheckSourcesForItem.setAccessible(true);
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getSourceName() {
        return "ncl";
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        if (!reference.identifier.startsWith("ncl!")) return null;
        NCLDelegateAbsorbingLoadHandler handler = new NCLDelegateAbsorbingLoadHandler();
        try {
            boolean success = (boolean)mCheckSourcesForItem.invoke(manager,
                    new AudioReference(reference.identifier.substring(4), reference.title), handler, new boolean[1]);
            if (!success || handler.result == null) return null;
            switch (handler.resultType) {
                case TRACK:
                    return new NCLAudioTrack((InternalAudioTrack)handler.result, this);
                case PLAYLIST:
                    return new NCLPlaylist((AudioPlaylist)handler.result, this);
            }
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        AudioTrack delegate = ((NCLAudioTrack)track).delegate;
        return delegate.getSourceManager().isTrackEncodable(delegate);
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        AudioTrack delegate = ((NCLAudioTrack)track).delegate;
        output.writeUTF(delegate.getSourceManager().getSourceName());
        delegate.getSourceManager().encodeTrack(delegate, output);
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        String sourceName = input.readUTF();
        for (AudioSourceManager manager : sourceManagers) {
            if (manager.getSourceName().equals(sourceName)) return manager.decodeTrack(trackInfo, input);
        }
        throw new IOException("No such source: " + sourceName);
    }

    @Override
    public void shutdown() {
        // NO-OP
    }

}
