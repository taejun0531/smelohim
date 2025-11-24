// 전역 테마 토글 (부드러운 전환 + 저장)
(function () {
    const STORAGE_KEY = "elohim-theme";
    const btn = document.getElementById("toggleTheme");

    // 현재 테마 적용
    function applyTheme(theme) {
        const isDark = theme === "dark";
        document.body.classList.toggle("dark", isDark);
        // 버튼 라벨/아이콘
        if (btn) {
            btn.textContent = isDark ? "🌞 라이트모드" : "🌙 다크모드";
            btn.setAttribute("aria-pressed", String(isDark));
        }
    }

    // 저장된 테마/시스템 선호도 반영
    const initialTheme = "light";
    applyTheme(initialTheme);

    // 전환 시 페이드 클래스 잠깐 부여 (iOS 등에서 더 안정적)
    function withFade(fn) {
        document.body.classList.add("theme-fade");
        fn();
        window.setTimeout(() => document.body.classList.remove("theme-fade"), 550);
    }

    // 버튼 이벤트
    if (btn) {
        btn.addEventListener("click", () => {
            const next = document.body.classList.contains("dark") ? "light" : "dark";
            withFade(() => applyTheme(next));
            localStorage.setItem(STORAGE_KEY, next);
        });
    }

    // 시스템 테마 변화도 반영(사용자가 저장을 안했을 때)
    const mm = window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)");
    if (mm) {
        mm.addEventListener("change", (e) => {
            withFade(() => applyTheme(e.matches ? "dark" : "light"));
        });
    }

    // 공통: 문서 타이틀 세팅
    try {
        const date = new Date();
        document.title = date.getFullYear() + "년도 엘로힘 청년부 관리사이트";
    } catch {}
})();
