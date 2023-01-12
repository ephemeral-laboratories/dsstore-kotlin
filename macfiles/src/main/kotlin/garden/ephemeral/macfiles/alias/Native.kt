package garden.ephemeral.macfiles.alias

import java.io.File

object Native {
    fun aliasForFile(path: File): Alias {
        TODO("Native code for alias generation")
        /*
        """Create an :class:`Alias` that points at the specified file."""
        if sys.platform != "darwin":
            raise Exception("Not implemented (requires special support)")

        path = encode_utf8(path)

        a = Alias()

        # Find the filesystem
        st = osx.statfs(path)
        vol_path = st.f_mntonname

        # File and folder names in HFS+ are normalized to a form similar to NFD.
        # Must be normalized (NFD->NFC) before use to avoid unicode string comparison issues.
        vol_path = normalize("NFC", vol_path.decode("utf-8")).encode("utf-8")

        # Grab its attributes
        attrs = [osx.ATTR_CMN_CRTIME, osx.ATTR_VOL_NAME, 0, 0, 0]
        volinfo = osx.getattrlist(vol_path, attrs, 0)

        vol_crtime = volinfo[0]
        vol_name = encode_utf8(volinfo[1])

        # Also grab various attributes of the file
        attrs = [
            osx.ATTR_CMN_OBJTYPE
            | osx.ATTR_CMN_CRTIME
            | osx.ATTR_CMN_FNDRINFO
            | osx.ATTR_CMN_FILEID
            | osx.ATTR_CMN_PARENTID,
            0,
            0,
            0,
            0,
        ]
        info = osx.getattrlist(path, attrs, osx.FSOPT_NOFOLLOW)

        if info[0] == osx.VDIR:
            kind = ALIAS_KIND_FOLDER
        else:
            kind = ALIAS_KIND_FILE

        cnid = info[3]
        folder_cnid = info[4]

        dirname, filename = os.path.split(path)

        if dirname == b"" or dirname == b".":
            dirname = os.getcwd()

        foldername = os.path.basename(dirname)

        creation_date = info[1]

        if kind == ALIAS_KIND_FILE:
            creator_code = struct.pack(b"I", info[2].fileInfo.fileCreator)
            type_code = struct.pack(b"I", info[2].fileInfo.fileType)
        else:
            creator_code = b"\0\0\0\0"
            type_code = b"\0\0\0\0"

        a.target = TargetInfo(
            kind, filename, folder_cnid, cnid, creation_date, creator_code, type_code
        )
        a.volume = VolumeInfo(vol_name, vol_crtime, b"H+", ALIAS_FIXED_DISK, 0, b"\0\0")

        a.target.folder_name = foldername
        a.volume.posix_path = vol_path

        rel_path = os.path.relpath(path, vol_path)

        # Leave off the initial '/' if vol_path is '/' (no idea why)
        if vol_path == b"/":
            a.target.posix_path = rel_path
        else:
            a.target.posix_path = b"/" + rel_path

        # Construct the Carbon and CNID paths
        carbon_path = []
        cnid_path = []
        head, tail = os.path.split(rel_path)
        if not tail:
            head, tail = os.path.split(head)
        while head or tail:
            if head:
                attrs = [osx.ATTR_CMN_FILEID, 0, 0, 0, 0]
                info = osx.getattrlist(os.path.join(vol_path, head), attrs, 0)
                cnid_path.append(info[0])
            carbon_tail = tail.replace(b":", b"/")
            carbon_path.insert(0, carbon_tail)
            head, tail = os.path.split(head)

        carbon_path = vol_name + b":" + b":\0".join(carbon_path)

        a.target.carbon_path = carbon_path
        a.target.cnid_path = cnid_path

        return a

         */
    }
}