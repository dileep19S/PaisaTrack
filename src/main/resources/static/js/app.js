/**
 * PaisaTrack - Client-side UI Enhancements
 * Note: Zero financial records are ever saved to localStorage, sessionStorage, or IndexedDB.
 */

(function () {
  'use strict';

  // 1. Register Static-Asset-Only PWA Service Worker
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      navigator.serviceWorker.register('/sw.js').catch(() => {
        // Registration failure is non-fatal
      });
    });
  }

  // 2. Online / Offline Connectivity Banner
  const offlineBanner = document.getElementById('offline-banner');
  function syncConnectivityBanner() {
    if (!offlineBanner) return;
    offlineBanner.hidden = navigator.onLine;
  }
  window.addEventListener('online', syncConnectivityBanner);
  window.addEventListener('offline', syncConnectivityBanner);
  syncConnectivityBanner();

  // 3. In-App PWA Install Prompt (Chromium beforeinstallprompt + iOS Safari fallback)
  let deferredInstallPrompt = null;
  const installButtons = document.querySelectorAll('[data-pwa-install]');
  const iosModal = document.getElementById('ios-install-modal');
  const iosCloseBtn = document.getElementById('ios-install-close');

  const isStandalone =
    window.matchMedia('(display-mode: standalone)').matches ||
    window.navigator.standalone === true;

  const isIOS = /iphone|ipad|ipod/i.test(window.navigator.userAgent);

  function updateInstallButtons() {
    installButtons.forEach((btn) => {
      if (isStandalone) {
        btn.hidden = true;
      } else if (deferredInstallPrompt || isIOS) {
        btn.hidden = false;
      } else {
        btn.hidden = true;
      }
    });
  }

  window.addEventListener('beforeinstallprompt', (event) => {
    event.preventDefault();
    deferredInstallPrompt = event;
    updateInstallButtons();
  });

  window.addEventListener('appinstalled', () => {
    deferredInstallPrompt = null;
    installButtons.forEach((btn) => {
      btn.hidden = true;
    });
  });

  installButtons.forEach((btn) => {
    btn.addEventListener('click', async () => {
      if (deferredInstallPrompt) {
        deferredInstallPrompt.prompt();
        const result = await deferredInstallPrompt.userChoice;
        if (result && result.outcome === 'accepted') {
          deferredInstallPrompt = null;
          updateInstallButtons();
        }
      } else if (isIOS && iosModal) {
        iosModal.hidden = false;
      }
    });
  });

  if (iosCloseBtn && iosModal) {
    iosCloseBtn.addEventListener('click', () => {
      iosModal.hidden = true;
    });
    iosModal.addEventListener('click', (e) => {
      if (e.target === iosModal) {
        iosModal.hidden = true;
      }
    });
  }

  updateInstallButtons();

  // 4. Dynamic Category Filtering on Add/Edit Transaction Form
  const typeRadios = document.querySelectorAll('input[name="transactionType"]');
  const categorySelect = document.getElementById('categoryId');

  if (typeRadios.length > 0 && categorySelect) {
    const allOptions = Array.from(categorySelect.querySelectorAll('option[data-type]'));

    function filterCategoriesForType(selectedType) {
      let firstVisibleValue = '';
      let currentStillVisible = false;

      allOptions.forEach((opt) => {
        const matches = opt.getAttribute('data-type') === selectedType;
        opt.hidden = !matches;
        opt.disabled = !matches;
        if (matches) {
          if (!firstVisibleValue) {
            firstVisibleValue = opt.value;
          }
          if (opt.value === categorySelect.value) {
            currentStillVisible = true;
          }
        }
      });

      if (!currentStillVisible) {
        categorySelect.value = firstVisibleValue;
      }
    }

    const checkedRadio = document.querySelector('input[name="transactionType"]:checked');
    if (checkedRadio) {
      filterCategoriesForType(checkedRadio.value);
    }

    typeRadios.forEach((radio) => {
      radio.addEventListener('change', (e) => {
        filterCategoriesForType(e.target.value);
      });
    });
  }

  // 5. Accessible Delete Confirmation Modal (replaces native browser confirm/alert)
  const deleteModal = document.getElementById('delete-confirm-modal');
  const deleteCancelBtn = document.getElementById('delete-cancel-btn');
  const deleteConfirmBtn = document.getElementById('delete-confirm-btn');
  let pendingDeleteForm = null;

  document.querySelectorAll('form[data-confirm-delete]').forEach((form) => {
    form.addEventListener('submit', (event) => {
      if (!deleteModal) return;
      event.preventDefault();
      pendingDeleteForm = form;
      deleteModal.hidden = false;
    });
  });

  if (deleteCancelBtn && deleteModal) {
    deleteCancelBtn.addEventListener('click', () => {
      pendingDeleteForm = null;
      deleteModal.hidden = true;
    });
    deleteModal.addEventListener('click', (e) => {
      if (e.target === deleteModal) {
        pendingDeleteForm = null;
        deleteModal.hidden = true;
      }
    });
  }

  if (deleteConfirmBtn) {
    deleteConfirmBtn.addEventListener('click', () => {
      if (pendingDeleteForm) {
        const formToSubmit = pendingDeleteForm;
        pendingDeleteForm = null;
        deleteModal.hidden = true;
        formToSubmit.submit();
      }
    });
  }
})();
