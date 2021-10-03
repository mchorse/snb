package mchorse.snb.utils;

import mchorse.mclib.utils.files.FileTree;
import mchorse.mclib.utils.files.entries.FolderImageEntry;

import java.io.File;

public class SnBTree extends FileTree
{
    public SnBTree(File folder)
    {
        this.root = new FolderImageEntry("s&b", folder, null);
    }
}