package garden.ephemeral.macfiles.bookmark

import java.io.File

object Native {
    fun bookmarkForFile(path: File): Bookmark {
        TODO("Native code for bookmark generation")
        /*
         @classmethod
         def for_file(cls, path):
             """Construct a :class:`Bookmark` for a given file."""

             # Find the filesystem
             st = osx.statfs(path)
             vol_path = st.f_mntonname.decode("utf-8")

             # Grab its attributes
             attrs = [
                 osx.ATTR_CMN_CRTIME,
                 osx.ATTR_VOL_SIZE | osx.ATTR_VOL_NAME | osx.ATTR_VOL_UUID,
                 0,
                 0,
                 0,
                 ]
             volinfo = osx.getattrlist(vol_path, attrs, 0)

             vol_crtime = volinfo[0]
             vol_size = volinfo[1]
             vol_name = volinfo[2]
             vol_uuid = volinfo[3]

             # Also grab various attributes of the file
             attrs = [
                 (osx.ATTR_CMN_OBJTYPE | osx.ATTR_CMN_CRTIME | osx.ATTR_CMN_FILEID),
                 0,
                 0,
                 0,
                 0,
             ]
             info = osx.getattrlist(path, attrs, osx.FSOPT_NOFOLLOW)

             cnid = info[2]
             crtime = info[1]

             if info[0] == osx.VREG:
                 flags = kCFURLResourceIsRegularFile
             elif info[0] == osx.VDIR:
                 flags = kCFURLResourceIsDirectory
             elif info[0] == osx.VLNK:
                 flags = kCFURLResourceIsSymbolicLink
             else:
                 flags = kCFURLResourceIsRegularFile

             dirname, filename = os.path.split(path)

             relcount = 0
             if not os.path.isabs(dirname):
                 curdir = os.getcwd()
                 head, tail = os.path.split(curdir)
                 relcount = 0
                 while head and tail:
                     relcount += 1
                     head, tail = os.path.split(head)
                 dirname = os.path.join(curdir, dirname)

             # ?? foldername = os.path.basename(dirname)

             rel_path = os.path.relpath(path, vol_path)

             # Build the path arrays
             name_path = []
             cnid_path = []
             head, tail = os.path.split(rel_path)
             if not tail:
                 head, tail = os.path.split(head)
             while head or tail:
                 if head:
                     attrs = [osx.ATTR_CMN_FILEID, 0, 0, 0, 0]
                     info = osx.getattrlist(os.path.join(vol_path, head), attrs, 0)
                     cnid_path.insert(0, info[0])
                     head, tail = os.path.split(head)
                     name_path.insert(0, tail)
                 else:
                     head, tail = os.path.split(head)
             name_path.append(filename)
             cnid_path.append(cnid)

             url_lengths = [relcount, len(name_path) - relcount]

             fileprops = Data(struct.pack(b"<QQQ", flags, 0x0F, 0))
             volprops = Data(
                 struct.pack(
                     b"<QQQ",
                     0x81 | kCFURLVolumeSupportsPersistentIDs,
                     0x13EF | kCFURLVolumeSupportsPersistentIDs,
                     0,
                     )
             )

             toc = {
                 kBookmarkPath: name_path,
                 kBookmarkCNIDPath: cnid_path,
                 kBookmarkFileCreationDate: crtime,
                 kBookmarkFileProperties: fileprops,
                 kBookmarkContainingFolder: len(name_path) - 2,
                 kBookmarkVolumePath: vol_path,
                 kBookmarkVolumeIsRoot: vol_path == "/",
                 kBookmarkVolumeURL: URL("file://" + vol_path),
                 kBookmarkVolumeName: vol_name,
                 kBookmarkVolumeSize: vol_size,
                 kBookmarkVolumeCreationDate: vol_crtime,
                 kBookmarkVolumeUUID: str(vol_uuid).upper(),
                 kBookmarkVolumeProperties: volprops,
                 kBookmarkCreationOptions: 512,
                 kBookmarkWasFileReference: True,
                 kBookmarkUserName: "unknown",
                 kBookmarkUID: 99,
             }

             if relcount:
                 toc[kBookmarkURLLengths] = url_lengths

             return Bookmark([(1, toc)])
          */
    }
}