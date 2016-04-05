package com.goeuro.app;

public class Main
{
	public static void main(String[] args)
	{
		String location = (args == null || args.length == 0) ? null : args[0];
		if (location == null || location.trim().length() == 0)
		{
			System.err.println("No locationprovided");
			return;
		}
		AppService appService = new AppService();
		appService.generateCSV(location);

	}
}
