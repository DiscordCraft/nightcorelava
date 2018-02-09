package io.github.phantamanta44.ncl;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.fftw3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NightcoreProcessor {

    private static final double FWD_SCALE = 2D / Short.MAX_VALUE;
    private static final double REV_SCALE = Short.MAX_VALUE / 2D;

    private static boolean fftwUnloaded = true;

    private static void tryLoadFftw() {
        if (fftwUnloaded) {
            fftwUnloaded = false;
            Loader.load(fftw3.class);
        }
    }

    private float freqFactor = 1.2F;

    public void setFrequencyFactor(float factor) {
        this.freqFactor = factor;
    }

    AudioFrame transform(AudioFrame frame) {
        // preliminary checks
        if (frame.isTerminator() || freqFactor == 1) return frame;
        AudioDataFormat fmt = frame.format;
        if (fmt.codec == AudioDataFormat.Codec.OPUS) {
            throw new UnsupportedOperationException("What the heck is Opus?!?");
        }

        // decode pcm data
        ByteOrder order = fmt.codec == AudioDataFormat.Codec.PCM_S16_BE
                ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        ByteBuffer buf = ByteBuffer.wrap(frame.data);
        buf.order(order);
        double[] data = new double[frame.data.length / Short.BYTES];
        for (int i = 0; i < data.length; i++) data[i] = buf.getShort(i) * FWD_SCALE;

        // perform forwards fourier transform
        tryLoadFftw();
        double[] result = new double[data.length / 2 + 1];
        fftw3.fftw_plan plan = fftw3.fftw_plan_dft_r2c_1d(data.length, data, result, (int)fftw3.FFTW_ESTIMATE);
        fftw3.fftw_execute(plan);
        fftw3.fftw_destroy_plan(plan);

        // mess with frequencies
        double[] processed = new double[result.length];
        int bound;
        if (freqFactor > 1) {
            bound = (int)Math.floor(result.length / freqFactor);
        } else {
            bound = result.length;
        }
        for (int i = 0; i < bound; i++) processed[(int)Math.floor(i * freqFactor)] = result[i];

        // perform inverse fourier transform
        plan = fftw3.fftw_plan_dft_c2r_1d(data.length, processed, data, (int)fftw3.FFTW_ESTIMATE);
        fftw3.fftw_execute(plan);
        fftw3.fftw_destroy_plan(plan);

        // encode pcm data
        buf = ByteBuffer.allocate(data.length * Short.BYTES);
        buf.order(order);
        for (int i = 0; i < data.length; i++) buf.putShort(i, (short)(result[i] * REV_SCALE));
        return new AudioFrame(frame.timecode, buf.array(), frame.volume, frame.format);
    }

}
