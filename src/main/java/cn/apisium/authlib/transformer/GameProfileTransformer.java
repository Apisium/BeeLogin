package cn.apisium.authlib.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.UUID;

import cn.apisium.authlib.GameProfile;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;

public class GameProfileTransformer implements ClassFileTransformer {

	@Override
	public byte[] transform(ClassLoader loader, String originalName, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		String className = originalName.replace("/", ".");
		switch (className) {
		case "com.mojang.authlib.GameProfile":
			return transformGameProfile(className, classfileBuffer);
		case "net.minecraft.server.v1_12_R1.PacketLoginOutSetCompression":
		case "net.minecraft.server.v1_12_R1.PacketLoginOutSuccess":
			return transformPacketSuccess(className, classfileBuffer);
		default:
			if (className.contains("com.mojang.authlib.GameProfile") && !className.contains("GameProfileRepository")) {
				return transformGameProfile(className, classfileBuffer);
			}
			break;
		}
		return null;
	}

	private byte[] transformPacketSuccess(String className, byte[] classfileBuffer) {
		try {
			ClassPool pool = ClassPool.getDefault();
			CtClass gameProfileClass;
			gameProfileClass = pool.get(className);
			for (CtMethod m : gameProfileClass.getDeclaredMethods()) {
				if (m.getParameterTypes().length < 1) {
					break;
				}
				try {
					m.insertBefore("System.out.println($1);");
					System.out.println("catcher dui" + m);
				} catch (CannotCompileException e) {
					e.printStackTrace();
				}
			}
			System.out.println("catcher dui1");
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private byte[] transformGameProfile(String className, byte[] original) {
		try {
			ClassPool pool = ClassPool.getDefault();
			CtClass gameProfileClass = pool.get(className);
			System.out.println(className);
			CtConstructor constructor = gameProfileClass.getDeclaredConstructors()[0];
			System.out.println(constructor);
			StringBuilder source = new StringBuilder();
			source = new StringBuilder();
			source.append(UUID.class.getName()).append(" temp = ").append(GameProfile.class.getName())
					.append(".getID($1, $2);");
			source.append("$2 = ").append(GameProfile.class.getName()).append(".getName($1, $2);");
			source.append("$1 = temp;");
			constructor.insertBefore(source.toString());
			gameProfileClass.writeFile();
			original = gameProfileClass.toBytecode();
			System.out.println("Injection successed.");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return original;
	}

}
