package com.zsxj.pda.scan;

import java.util.HashMap;



import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundUtil {
	// 音效名字的常量
		public static final int SOUNG_BMG_FIRE = 1;
		// 单例模式
		private static SoundUtil manager;
		// 声音池
		private SoundPool soundPool;
		// 创建一个HashMap用于保存资源名与声音池分配的资源id建立的联系
		private HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();

		public static SoundUtil getSoundManager(Context context) {
			if (manager == null) {
				manager = new SoundUtil(context);
			}
			return manager;
		}

		/**
		 * 初始化
		 */
		private SoundUtil(Context context) {
			try {
				// 初始化声音池,三个参数,同时最多可以播放多少个、流类型、0
				soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
				// 向池中添加音效,三个参数、第三个现在还不知道有什么作用
//				soundMap.put(SOUNG_BMG_FIRE, soundPool.load(context, R.raw.ding, -1));
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * 根据SoundManager的音效常量,播放对应的音效
		 * 
		 * 
		 * @param soundId
		 *            SoundManager中定义的音效常量
		 */
		public void playSound(int soundId) {
			soundPool.play(soundMap.get(soundId), 1, 1, 0, 0, 1);
		}

		/**
		 * 释放资源
		 */
		public static void release() {
			if (manager != null && manager.soundPool != null) {
				manager.soundPool.release();
			}
			manager = null;
		}
}
