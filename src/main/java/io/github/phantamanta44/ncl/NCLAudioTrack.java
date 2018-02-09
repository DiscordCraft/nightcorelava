package io.github.phantamanta44.ncl;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioTrackExecutor;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NCLAudioTrack implements InternalAudioTrack {

    final InternalAudioTrack delegate;
    private final AudioSourceManager manager;
    private final NightcoreProcessor ncp;

    public NCLAudioTrack(InternalAudioTrack delegate, AudioSourceManager manager) {
        this.delegate = delegate;
        this.manager = manager;
        this.ncp = new NightcoreProcessor();
    }

    public NightcoreProcessor getNightcore() {
        return ncp;
    }

    @Override
    public AudioTrackInfo getInfo() {
        return delegate.getInfo();
    }

    @Override
    public String getIdentifier() {
        return "ncl!" + delegate.getIdentifier();
    }

    @Override
    public AudioTrackState getState() {
        return delegate.getState();
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    public boolean isSeekable() {
        return delegate.isSeekable();
    }

    @Override
    public long getPosition() {
        return delegate.getPosition();
    }

    @Override
    public void setPosition(long position) {
        delegate.setPosition(position);
    }

    @Override
    public void setMarker(TrackMarker marker) {
        delegate.setMarker(marker);
    }

    @Override
    public long getDuration() {
        return delegate.getDuration();
    }

    @Override
    public AudioTrack makeClone() {
        return new NCLAudioTrack((InternalAudioTrack)delegate.makeClone(), manager);
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return manager;
    }

    @Override
    public void setUserData(Object userData) {
        delegate.setUserData(userData);
    }

    @Override
    public Object getUserData() {
        return delegate.getUserData();
    }

    @Override
    public <T> T getUserData(Class<T> clazz) {
        return delegate.getUserData(clazz);
    }

    @Override
    public void assignExecutor(AudioTrackExecutor executor, boolean applyPrimordialState) {
        delegate.assignExecutor(executor, applyPrimordialState);
    }

    @Override
    public AudioTrackExecutor getActiveExecutor() {
        return delegate.getActiveExecutor();
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        delegate.process(executor);
    }

    @Override
    public AudioTrackExecutor createLocalExecutor(AudioPlayerManager manager) {
        return delegate.createLocalExecutor(manager);
    }

    @Override
    public AudioFrame provide() {
        return ncp.transform(delegate.provide());
    }

    @Override
    public AudioFrame provide(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        return ncp.transform(delegate.provide(timeout, unit));
    }

}
