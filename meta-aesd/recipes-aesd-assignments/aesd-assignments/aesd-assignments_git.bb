#__License Info__
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

#__Github repo url to be cloned__
SRC_URI = "git://github.com/cu-ecen-aeld/Programs_for_Server_and_Device_Driver.git;protocol=ssh;branch=main"


SRCREV = "706acaf6682921cde6edd2d5eaf990518ee390c7"

PV = "1.0+git${SRCPV}"
# SRCPV expands to a short hash from SRCREV (aesd-assignments-1.0+git1f993ece38)


#__The Source Directory__
S = "${WORKDIR}/git"  

#__Libraries needed__
TARGET_LDFLAGS += "-pthread -lrt"
# Dependencies (Build-Time)
DEPENDS += "libgcc glibc" 
# Dependencies (Runtime)
RDEPENDS:${PN} += "libgcc glibc" 

#__Inherit Yocto build system support__
inherit module-base kernel-module-split pkgconfig


EXTRA_OEMAKE = "HOST=0 SYSROOT=${STAGING_DIR_TARGET} KERNELDIR=${STAGING_KERNEL_DIR}"

# Packages to be created
# PACKAGES += "kernel-module-aesdchar"

# Runtime dependency on kernel module
RDEPENDS:${PN} += "kernel-module-aesdchar"


#__Files to export to target__
FILES:${PN} += "${bindir}/aesdsocket" 
FILES:${PN} += "${sysconfdir}/init.d/S99aesdsocket"

FILES:${PN} += "${sysconfdir}/init.d/S90aesdchar"
FILES:${PN} += "/usr/bin/aesdchar_load"
FILES:${PN} += "/usr/bin/aesdchar_unload"
FILES:${PN} += "/etc/rcS.d/S90aesdchar"
FILES:${PN} += "/etc/rcS.d/S99aesdsocket"
FILES:${PN} += "/etc/rc6.d/K99aesdsocket"
FILES:${PN} += "/etc/rc6.d/K90aesdchar"
FILES:${PN} += "/etc/rc0.d/K99aesdsocket"
FILES:${PN} += "/etc/rc0.d/K90aesdchar"

FILES:kernel-module-aesdchar = "/lib/modules/*/extra/aesdchar.ko"




do_configure () {
    :     
}
# : is a no-op, yocto will skip this step

#__Yocto's recipe build step__
do_compile () {
    oe_runmake -C ${S}/server
    oe_runmake -C ${S}/aesd-char-driver KERNEL_SRC=${STAGING_KERNEL_DIR}
}

#__Yocto's install to target step__
do_install() {
    # Install kernel module
    install -d ${D}/lib/modules/${KERNEL_VERSION}/extra
    install -m 0644 ${S}/aesd-char-driver/aesdchar.ko ${D}/lib/modules/${KERNEL_VERSION}/extra/

    # Install binaries
    install -d ${D}${bindir}
    install -m 0755 ${S}/server/aesdsocket ${D}${bindir}
    install -m 0755 ${S}/aesd-char-driver/aesdchar_load ${D}${bindir}
    install -m 0755 ${S}/aesd-char-driver/aesdchar_unload ${D}${bindir}

    # Install init scripts
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${S}/aesd-char-driver/aesdchar-start-stop.sh ${D}${sysconfdir}/init.d/S90aesdchar
    install -m 0755 ${S}/server/aesdsocket-start-stop.sh ${D}${sysconfdir}/init.d/S99aesdsocket

    # Install runlevel symlinks
    for runlevel in rcS rc0 rc6; do
        install -d ${D}${sysconfdir}/${runlevel}.d
    done

    ln -sf ${sysconfdir}/init.d/S90aesdchar    ${D}${sysconfdir}/rcS.d/S90aesdchar
    ln -sf ${sysconfdir}/init.d/S99aesdsocket  ${D}${sysconfdir}/rcS.d/S99aesdsocket
    ln -sf ${sysconfdir}/init.d/S90aesdchar    ${D}${sysconfdir}/rc0.d/K90aesdchar
    ln -sf ${sysconfdir}/init.d/S99aesdsocket  ${D}${sysconfdir}/rc0.d/K99aesdsocket
    ln -sf ${sysconfdir}/init.d/S90aesdchar    ${D}${sysconfdir}/rc6.d/K90aesdchar
    ln -sf ${sysconfdir}/init.d/S99aesdsocket  ${D}${sysconfdir}/rc6.d/K99aesdsocket
}


