/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

#include <windows.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>

#include <jni.h>
#include "java.h"

#ifdef DEBUG
#define JVM_DLL "jvm_g.dll"
#define JAVA_DLL "java_g.dll"
#else
#define JVM_DLL "jvm.dll"
#define JAVA_DLL "java.dll"
#endif

/*
 * Prototypes.
 */
static jboolean
GetPublicJREHome(char *path, jint pathsize);

/*
 * If the target VM is a symbolic link to another valid VM, return a pointer
 * to the name of that VM.  If the target VM is a link to something else
 * (not to be documented?) return an empty string.  Otherwise return NULL.
 * NOTE: win32 does not have symlinks, so this is a no-op.
 */

const char *
ReadJVMLink(const char *jrepath, const char *jvmtype,
	    char* knownVMs[], int knownVMsCount) {
    return NULL;
}

/*
 * Find path to JRE based on .exe's location or registry settings.
 */
jboolean
GetJREPath(char *path, jint pathsize)
{
    char javadll[MAXPATHLEN];
    struct stat s;

    if (GetApplicationHome(path, pathsize)) {
	/* Is JRE co-located with the application? */
	sprintf(javadll, "%s\\bin\\" JAVA_DLL, path);
	if (stat(javadll, &s) == 0) {
	    goto found;
	}

	/* Does this app ship a private JRE in <apphome>\jre directory? */
	sprintf(javadll, "%s\\jre\\bin\\" JAVA_DLL, path);
	if (stat(javadll, &s) == 0) {
	    strcat(path, "\\jre");
	    goto found;
	}
    }

    /* Look for a public JRE on this machine. */
    if (GetPublicJREHome(path, pathsize)) {
	goto found;
    }

    fprintf(stderr, "Error: could not find " JAVA_DLL "\n");
    return JNI_FALSE;

 found:
    if (debug) printf("JRE path is %s\n", path);
    return JNI_TRUE;
}

/*
 * Given a JRE location and a JVM type, construct what the name the
 * JVM shared library will be.  Return true, if such a library
 * exists, false otherwise.
 */
jboolean
GetJVMPath(const char *jrepath, const char *jvmtype,
	   char *jvmpath, jint jvmpathsize)
{
    struct stat s;
    sprintf(jvmpath, "%s\\bin\\%s\\" JVM_DLL, jrepath, jvmtype);
    if (stat(jvmpath, &s) == 0) {
	return JNI_TRUE;
    } else {
	return JNI_FALSE;
    }
}

/*
 * Load a jvm from "jvmpath" and intialize the invocation functions.
 */
jboolean
LoadJavaVM(const char *jvmpath, InvocationFunctions *ifn)
{
    HINSTANCE handle;

    if (debug) {
	printf("JVM path is %s\n", jvmpath);
    }

    /* Load the Java VM DLL */
    if ((handle = LoadLibrary(jvmpath)) == 0) {
	fprintf(stderr, "Error loading: %s\n", jvmpath);
	return JNI_FALSE;
    }

    /* Now get the function addresses */
    ifn->CreateJavaVM =
	(void *)GetProcAddress(handle, "JNI_CreateJavaVM");
    ifn->GetDefaultJavaVMInitArgs =
	(void *)GetProcAddress(handle, "JNI_GetDefaultJavaVMInitArgs");
    if (ifn->CreateJavaVM == 0 || ifn->GetDefaultJavaVMInitArgs == 0) {
	fprintf(stderr, "Error: can't find JNI interfaces in: %s\n", jvmpath);
	return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * Get the path to the file that has the usage message for -X options.
 */
void
GetXUsagePath(char *buf, jint bufsize)
{
    GetModuleFileName(GetModuleHandle(JVM_DLL), buf, bufsize);
    *(strrchr(buf, '\\')) = '\0';
    strcat(buf, "\\Xusage.txt");
}

/*
 * If app is "c:\foo\bin\javac", then put "c:\foo" into buf.
 */
jboolean
GetApplicationHome(char *buf, jint bufsize)
{
    char *cp;
    GetModuleFileName(0, buf, bufsize);
    *strrchr(buf, '\\') = '\0'; /* remove .exe file name */
    if ((cp = strrchr(buf, '\\')) == 0) {
	/* This happens if the application is in a drive root, and
	 * there is no bin directory. */
	buf[0] = '\0';
	return JNI_FALSE;
    }
    *cp = '\0';  /* remove the bin\ part */
    return JNI_TRUE;
}

#ifdef JAVAW
__declspec(dllimport) char **__initenv;
extern int status;

int WINAPI
WinMain(HINSTANCE inst, HINSTANCE previnst, LPSTR cmdline, int cmdshow)
{
    char* message;
    int   ret;

    __initenv = _environ;
    ret = main(__argc, __argv);

    switch (status) {
    case 1:
        message = "Could not load the Java virtual machine. Program will exit!";
        break;
    case 2:
        message = "Could not parse the command arguments. Program will exit!";
        break;
    case 3:
        message = "Could not initialize the Java virtual machine. Program will exit!";
        break;
    case 4:
        message = "Could not find the main class. Program will exit!";
        break;
    case 5:
        message = "Could not find the main method. Program will exit!";
        break;
    default:
        message = NULL;
        break;
    }
    if (message) {
        MessageBox(NULL, message, "Java Virtual Machine Launcher",
            (MB_OK|MB_ICONSTOP|MB_APPLMODAL)); 
    }

    return ret; 
}
#endif

/*
 * Helpers to look in the registry for a public JRE.
 */
#define DOTRELEASE  "1.3" /* Same for 1.3.1, 1.3.2 etc. */
#define JRE_KEY	    "Software\\JavaSoft\\Java Runtime Environment"

static jboolean
GetStringFromRegistry(HKEY key, const char *name, char *buf, jint bufsize)
{
    DWORD type, size;

    if (RegQueryValueEx(key, name, 0, &type, 0, &size) == 0
	&& type == REG_SZ
	&& (size < (unsigned int)bufsize)) {
	if (RegQueryValueEx(key, name, 0, 0, buf, &size) == 0) {
	    return JNI_TRUE;
	}
    }
    return JNI_FALSE;
}

static jboolean
GetPublicJREHome(char *buf, jint bufsize)
{
    HKEY key, subkey;
    char version[MAXPATHLEN];

    /* Find the current version of the JRE */
    if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, JRE_KEY, 0, KEY_READ, &key) != 0) {
	fprintf(stderr, "Error opening registry key '" JRE_KEY "'\n");
	return JNI_FALSE;
    }

    if (!GetStringFromRegistry(key, "CurrentVersion",
			       version, sizeof(version))) {
	fprintf(stderr, "Failed reading value of registry key:\n\t"
		JRE_KEY "\\CurrentVersion\n");
	RegCloseKey(key);
	return JNI_FALSE;
    }

    if (strcmp(version, DOTRELEASE) != 0) {
	fprintf(stderr, "Registry key '" JRE_KEY "\\CurrentVersion'\nhas "
		"value '%s', but '" DOTRELEASE "' is required.\n", version);
	RegCloseKey(key);
	return JNI_FALSE;
    }

    /* Find directory where the current version is installed. */
    if (RegOpenKeyEx(key, version, 0, KEY_READ, &subkey) != 0) {
	fprintf(stderr, "Error opening registry key '"
		JRE_KEY "\\%s'\n", version);
	RegCloseKey(key);
	return JNI_FALSE;
    }

    if (!GetStringFromRegistry(subkey, "JavaHome", buf, bufsize)) {
	fprintf(stderr, "Failed reading value of registry key:\n\t"
		JRE_KEY "\\%s\\JavaHome\n", version);
	RegCloseKey(key);
	RegCloseKey(subkey);
	return JNI_FALSE;
    }

    if (debug) {
	char micro[MAXPATHLEN];
	if (!GetStringFromRegistry(subkey, "MicroVersion", micro,
				   sizeof(micro))) {
	    printf("Warning: Can't read MicroVersion\n");
	    micro[0] = '\0';
	}
	printf("Version major.minor.micro = %s.%s\n", version, micro);
    }

    RegCloseKey(key);
    RegCloseKey(subkey);
    return JNI_TRUE;
}

/*
 * Support for doing cheap, accurate interval timing.
 */
static jboolean counterAvailable = JNI_FALSE;
static jboolean counterInitialized = JNI_FALSE;
static LARGE_INTEGER counterFrequency;

jlong CounterGet()
{
    LARGE_INTEGER count;

    if (!counterInitialized) {
	counterAvailable = QueryPerformanceFrequency(&counterFrequency);
	counterInitialized = JNI_TRUE;
    }
    if (!counterAvailable) {
	return 0;
    }
    QueryPerformanceCounter(&count);
    return (jlong)(count.QuadPart);
}

jlong Counter2Micros(jlong counts)
{
    if (!counterAvailable || !counterInitialized) {
	return 0;
    }
    return (counts * 1000 * 1000)/counterFrequency.QuadPart;
}

