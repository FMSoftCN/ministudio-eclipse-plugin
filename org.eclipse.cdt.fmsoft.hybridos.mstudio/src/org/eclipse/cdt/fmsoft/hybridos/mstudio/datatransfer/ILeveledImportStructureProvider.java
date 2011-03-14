package org.eclipse.cdt.fmsoft.hybridos.mstudio.datatransfer;

/**
 * Interface which can provide structure and content information for an archive 
 * element. Used by the import wizards to abstract the commonalities between 
 * importing from the a zip file and importing from a tar file.
 * 
 * @since 3.1
 */
public interface ILeveledImportStructureProvider extends IImportStructureProvider {
	/**
	 * Returns the entry that this importer uses as the root sentinel.
	 * 
	 * @return root entry of the archive file
	 */
	public abstract Object getRoot();

	/**
	 * Tells the provider to strip N number of directories from the path of any
	 * path or file name returned by the IImportStructureProvider (Default=0).
	 * 
	 * @param level
	 *            The number of directories to strip
	 */
	public abstract void setStrip(int level);

	/**
	 * Returns the number of directories that this IImportStructureProvider is
	 * stripping from the file name
	 * 
	 * @return int Number of entries
	 */
	public abstract int getStrip();
	
	/**
	 * Close the archive file that was used to create this leveled structure provider.
	 * 
	 * @return <code>true</code> if the archive was closed successfully
	 */
	public boolean closeArchive();
}
