
/* see ../../../../../LICENSE for release details */
package com.beyondrelations.runtime.cwf;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;

import com.beyondrelations.runtime.cwf.model.Configuration;
import com.beyondrelations.runtime.cwf.model.MultiWorkfloRoot;
import com.beyondrelations.runtime.cwf.model.WorkfloRoot;
import com.beyondrelations.runtime.cwf.model.WorkfloSession;

/**

*/
public class ChecksForFiles
{

	private static final String cl = "cff.";


	/** Returns paths to jars themselves */
	public static Collection<Path> workfloFilesOf( MultiWorkfloRoot base )
	{
		final String here = cl +"wfo ";
		Collection<Path> relevantFolders = new LinkedList<>();
		try
		{
			File[] allChildrenOfRoot = base.getRootDir().toFile().listFiles(
					new FileFilter()
					{
						@Override
						public boolean accept( File candidate )
						{
							return candidate.isDirectory();
						}
					}
				);
			if ( allChildrenOfRoot == null )
				throw new RuntimeException( here +"invalid multiworkflo root, quit" );
			else if ( allChildrenOfRoot.length == 0 )
				throw new RuntimeException( here +"multi wf folder is empty "
						+ base.getRootDir() +", quit" );
			for ( File someChild : allChildrenOfRoot )
			{
				File[] jarsWithin = someChild.listFiles(
						new FilenameFilter()
						{
							@Override
							public boolean accept( File parentFolder, String name )
							{
								return name.endsWith( "jar" );
							}
						}
					);
				for ( File jar : jarsWithin )
				{
					relevantFolders.add( jar.toPath() );
				}
			}
		}
		catch ( SecurityException se )
		{
			System.err.println( here +" unable to list wf folders of "
					+ base.getRootDir() +" because "+ se );
		}
		return relevantFolders;
	}


	public static Collection<Path> configFilesOf(
			Path workfloJar, boolean isMicrotools )
	{
		final String here = cl +"cfo ";
		Path base = Paths.get( workfloJar.getParent().toString(), "config" );
		Collection<Path> relevantFiles = new LinkedList<>();
		try
		{
			final String extension = isMicrotools ? "properties" : "json";
			File[] dbInfoWithin = base.toFile().listFiles(
					new FilenameFilter()
					{
						@Override
						public boolean accept( File parentFolder, String name )
						{
							return name.endsWith( extension );
						}
					}
				);
			if ( dbInfoWithin == null )
				throw new RuntimeException( here +"config folder next to jar "
						+ workfloJar +", quit" );
			else if ( dbInfoWithin.length == 0 )
				throw new RuntimeException( here +"config folder empty for jar "
						+ workfloJar +", quit" );
			for ( File curr : dbInfoWithin )
			{
				relevantFiles.add( curr.toPath() );
			}
		}
		catch ( SecurityException se )
		{
			System.err.println( here +" unable to list wf folders of "
					+ base +" because "+ se );
		}
		return relevantFiles;
	}


}









































































