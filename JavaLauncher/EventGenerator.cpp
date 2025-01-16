/*
This application launches the Java classes of 
the NETCOOL / Event Generator program.
*/

#define WIN32_LEAN_AND_MEAN
#define _WIN32_WINNT 0x0501
#include <windows.h>
#include <direct.h>
#include <stdlib.h>
#include <stdio.h>
#include <shellapi.h>

#define JRE_KEY TEXT("SOFTWARE\\JavaSoft\\Java Runtime Environment")
#define JRE_VER TEXT("CurrentVersion")
#define JRE_HOME TEXT("JavaHome")

int APIENTRY WinMain(HINSTANCE hInstance,
                     HINSTANCE hPrevInstance,
                     LPSTR     lpCmdLine,
                     int       nCmdShow)
{
	bool status = true;
	char *javahome;
	char *egenhome;
	char java_home[100];
	char regpath[100];
	char javaexec[100];
	char classpath[1000];
	
	// Get both environment variables from the system properties
	// ---------------------------------------------------------
	//Get java.home from the Windows registries
	HKEY			hKey			= NULL;
	unsigned long	datatype;
	LPBYTE			buffer;
	unsigned long	bufferlength	= 1024;

    // First, check for installed java.
	// Verify that this key exist and if it does, get its value (the java version)
	if(RegOpenKeyEx(HKEY_LOCAL_MACHINE, JRE_KEY, 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
		RegQueryValueEx(hKey, JRE_VER, 0, &datatype, NULL, &bufferlength);
		buffer = (LPBYTE)malloc(bufferlength);
		if (RegQueryValueEx(hKey, JRE_VER, 0, &datatype, buffer, &bufferlength) != ERROR_SUCCESS) {
			status = false;
		}
		RegCloseKey(hKey);
	} else {
		status = false;
	}
	
	// Create the path to the used java using the retrieved java version
	strcpy(regpath, JRE_KEY);
	strncat(regpath, "\\", 1);
	strncat(regpath, (const char*)buffer, strlen((const char*)buffer));
	//MessageBox(NULL, regpath, "DEBUG MESSAGE", MB_OK);

	// Second, get the java home location
	// Verify that this key exist and if it does, get its value (the java homa directory)
	if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, regpath, 0, KEY_QUERY_VALUE, &hKey) == ERROR_SUCCESS) {
		// Allocate buffer size
		RegQueryValueEx(hKey, JRE_HOME, 0, &datatype, NULL, &bufferlength);
		buffer = (LPBYTE)malloc(bufferlength);
		if(RegQueryValueEx(hKey, JRE_HOME, 0, &datatype, buffer, &bufferlength) != ERROR_SUCCESS) {
			status = false;
		} else {
			strcpy(java_home, (const char*)buffer);
		}
		RegCloseKey(hKey);
	} else {
		status = false;
	}
	//MessageBox(NULL, java_home, "DEBUG MESSAGE", MB_OK);
	
	if (status == false) {
		javahome = getenv("JAVA_HOME");
		if (javahome==NULL) {
			char temp[1000];
			sprintf(temp,"Java Runtime Environment not found. Please download and install\n" 
				"the latest JRE. Alteratively, if it is installed on your system,\n"
				"set JAVA_HOME environment variable to its location.", NULL);
			MessageBox(NULL, temp,"Error loading JRE", MB_OK);
			return 1;
		}
		strcpy(java_home, (const char*)javahome);
	}

	// Get programs library location
	egenhome = getenv("EGEN_HOME");

	if (egenhome==NULL) {
		char temp[1000];
		sprintf(temp,"Could not find EGEN_HOME environment variable. Please\n" 
			"verify that the EGEN_HOME is set as environment variable.", NULL);
		MessageBox(NULL, temp,"Error loading EGEN_HOME", MB_OK);
		return 1;
	}

	// Verify that the paths set in the ENV do exist.
	if (_chdir(java_home)) {
		char temp[1000];
		sprintf(temp, "Could not find the JAVA_HOME directory.", NULL);
		MessageBox(NULL, temp, "Error testing directory",MB_OK);
		return 2;
	}
	if (_chdir(egenhome)) {
		char temp[1000];
		sprintf(temp, "Could not find the EGEN_HOME directory.", NULL);
		MessageBox(NULL, temp, "Error testing directory",MB_OK);
		return 2;
	}
	
	// Prepare JAVA executable
	strcpy(javaexec, "\"");
	strncat(javaexec, java_home, strlen(java_home));
	strncat(javaexec, "\\bin\\java\"", 10);
	//MessageBox(NULL, javaexec, "DEBUG MESSAGE",MB_OK);
	
	// Prepare CLASSPATH + STARTCLASS
	const char *libs = egenhome;
	strcpy(classpath, "\0");
	strncat(classpath, " -splash:\"", 10);
	strncat(classpath, egenhome, strlen(egenhome));
	strncat(classpath, "\\images\\splash.jpg\" -DEGEN_HOME=\"", 33);
	strncat(classpath, egenhome, strlen(egenhome));
	strncat(classpath, "\" -cp \"", 7);
	strncat(classpath, libs, strlen(libs));
	strncat(classpath, "\\libs", 5);
	strncat(classpath, "\\sybjdbc.jar;", 13);
	strncat(classpath, libs, strlen(libs));
	strncat(classpath, "\\libs", 5);
	strncat(classpath, "\\Configurator.jar\"", 18);
	strncat(classpath, " gui.Configurator", 18);
	//MessageBox(NULL, classpath, "DEBUG MESSAGE",MB_OK);

	// Execute the program
	if (ShellExecute(NULL, NULL, javaexec, classpath, egenhome, SW_HIDE)<=(HINSTANCE)32) {
		MessageBox(NULL, "Could not start the application", "Please reinstall the program.",MB_OK);
		return 2; 
	}

	return 0;
}
