package com.coderGtm.yantra.gui.web

/** Injects the stable Yantra JavaScript API and custom-element behavior into GUI HTML. */
object YantraDomProcessor {

    fun inject(html: String): String {
        val runtime = """
            <script>
            (function() {
                const bridge = window.YantraAndroid;
                if (!bridge) return;

                const APP_NOT_FOUND = 'APP NOT FOUND. Reconfigure your GUI elements.';

                function parseApps() {
                    try {
                        return JSON.parse(bridge.appsList() || '[]');
                    } catch (e) {
                        return [];
                    }
                }

                window.yantra = {
                    apps: {
                        list: () => parseApps(),
                        exists: (pkg) => !!bridge.appExists(pkg),
                        launch: (pkg) => !!bridge.launchApp(pkg),
                        info: (pkg) => !!bridge.appInfo(pkg),
                        uninstall: (pkg) => !!bridge.uninstallApp(pkg)
                    },
                    shell: {
                        cli: () => bridge.shellCli(),
                        gui: () => bridge.shellGui()
                    },
                    drawer: {
                        open: () => bridge.drawerOpen(),
                        close: () => bridge.drawerClose()
                    },
                    device: {
                        lock: () => bridge.deviceLock()
                    },
                    wallpaper: {
                        get: () => bridge.wallpaperGet()
                    }
                };

                function markMissing(el) {
                    el.dataset.yantraError = APP_NOT_FOUND;
                    el.setAttribute('title', APP_NOT_FOUND);
                    el.classList.add('yantra-app-not-found');
                }

                function clearMissing(el) {
                    delete el.dataset.yantraError;
                    el.removeAttribute('title');
                    el.classList.remove('yantra-app-not-found');
                }

                function prepareApp(el) {
                    const pkg = el.getAttribute('package');
                    if (!pkg || el.dataset.yantraReady) return;

                    el.dataset.yantraReady = '1';
                    el.style.cursor = 'pointer';

                    if (!yantra.apps.exists(pkg)) {
                        markMissing(el);
                    }

                    el.addEventListener('click', function() {
                        if (!yantra.apps.launch(pkg)) {
                            markMissing(el);
                        } else {
                            clearMissing(el);
                        }
                    });
                }

                function prepareAppAction(el) {
                    const pkg = el.getAttribute('package');
                    const action = (el.getAttribute('action') || 'launch').toLowerCase();
                    if (!pkg || el.dataset.yantraReady) return;

                    el.dataset.yantraReady = '1';
                    el.style.cursor = 'pointer';

                    if (!yantra.apps.exists(pkg)) {
                        markMissing(el);
                    }

                    el.addEventListener('click', function() {
                        let ok;
                        if (action === 'info') ok = yantra.apps.info(pkg);
                        else if (action === 'uninstall') ok = yantra.apps.uninstall(pkg);
                        else ok = yantra.apps.launch(pkg);

                        if (ok) clearMissing(el);
                        else markMissing(el);
                    });
                }

                function prepareAction(el) {
                    const action = (el.getAttribute('action') || '').toLowerCase();
                    if (!action || el.dataset.yantraReady) return;

                    el.dataset.yantraReady = '1';
                    el.style.cursor = 'pointer';

                    el.addEventListener('click', function() {
                        switch (action) {
                            case 'cli':
                                yantra.shell.cli();
                                break;
                            case 'gui':
                                yantra.shell.gui();
                                break;
                            case 'drawer-open':
                                yantra.drawer.open();
                                break;
                            case 'drawer-close':
                                yantra.drawer.close();
                                break;
                            case 'lock':
                                yantra.device.lock();
                                break;
                        }
                    });
                }

                function prepareApps(el) {
                    const preset = (el.getAttribute('preset') || '').toLowerCase();
                    if (el.dataset.yantraReady) return;

                    el.dataset.yantraReady = '1';

                    if (preset !== 'all') {
                        el.dataset.yantraError = 'Unsupported preset: ' + preset;
                        return;
                    }

                    const apps = parseApps();
                    el.replaceChildren();

                    apps.forEach(function(app) {
                        const item = document.createElement('button');
                        item.type = 'button';
                        item.textContent = app.name;
                        item.dataset.package = app.package;
                        item.className = 'yantra-generated-app';
                        item.addEventListener('click', function() {
                            yantra.apps.launch(app.package);
                        });
                        el.appendChild(item);
                    });
                }

                function prepare(root) {
                    const scope = root || document;

                    if (scope !== document && scope.matches) {
                        if (scope.matches('yantra-app')) prepareApp(scope);
                        if (scope.matches('yantra-app-action')) prepareAppAction(scope);
                        if (scope.matches('yantra-action')) prepareAction(scope);
                        if (scope.matches('yantra-apps[preset]')) prepareApps(scope);
                    }

                    scope.querySelectorAll('yantra-app').forEach(prepareApp);
                    scope.querySelectorAll('yantra-app-action').forEach(prepareAppAction);
                    scope.querySelectorAll('yantra-action').forEach(prepareAction);
                    scope.querySelectorAll('yantra-apps[preset]').forEach(prepareApps);
                }

                function boot() {
                    prepare(document);
                    const observer = new MutationObserver(function(mutations) {
                        mutations.forEach(function(mutation) {
                            mutation.addedNodes.forEach(function(node) {
                                if (node.nodeType !== 1) return;
                                prepare(node);
                            });
                        });
                    });
                    observer.observe(document.documentElement, { childList: true, subtree: true });
                    document.dispatchEvent(new CustomEvent('yantra-ready'));
                }

                if (document.readyState === 'loading') {
                    document.addEventListener('DOMContentLoaded', boot, { once: true });
                } else {
                    boot();
                }
            })();
            </script>
        """.trimIndent()

        return if (html.contains("</head>", ignoreCase = true)) {
            html.replaceFirst(
                Regex("</head>", RegexOption.IGNORE_CASE),
                "$runtime\n</head>"
            )
        } else {
            runtime + html
        }
    }
}
