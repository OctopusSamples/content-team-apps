(function () {
  // Cache variables for increased performance on devices with slow CPUs.
  var flexContainer = document.querySelector('div.flex-container');
  var searchIcon = document.querySelector('.search-icon');
  var searchBox = document.querySelector('.search-box');
  var searchClose = document.querySelector('.search-icon-close');
  var searchInput = document.querySelector('#search-input');
  var activeClass = 'active';
  var searchActiveClass = 'search-active';

  function toggle(elem, className) {
    elem.classList.contains(className)
      ? elem.classList.remove(className)
      : elem.classList.add(className);
  }

  // Menu Settings
  document.querySelectorAll('.menu-icon, .menu-icon-close').forEach(function (elem) {
    elem.addEventListener('click', function (e) {
      e.preventDefault();
      e.stopPropagation();
      toggle(flexContainer, activeClass);
    });
  });

  // Click outside of menu to close it
  flexContainer.addEventListener('click', function (e) {
    if (flexContainer.classList.contains(activeClass) && e.target.tagName !== 'A') {
      flexContainer.classList.remove(activeClass)
    }
  });

  // Press Escape key to close menu
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
      flexContainer.classList.remove(activeClass);
      searchBox.classList.remove(searchActiveClass);
    }
  });

  // If elements not present, no need to continue with search
  if (!searchIcon) {
    return;
  }

  // Search Settings
  searchIcon.addEventListener('click', function (e) {
    e.preventDefault();
    toggle(searchBox, searchActiveClass);
    searchInput.focus();

    if (searchBox.classList.contains(searchActiveClass)) {
      searchClose.addEventListener('click', function (e) {
    		e.preventDefault();
    		searchBox.classList.remove(searchActiveClass);
    	});
    }
  });
})();
