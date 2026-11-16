/**
 * PaisaTrack - Expense Analytics Charts (Chart.js)
 * Renders category breakdown and monthly income vs expense using live MySQL data.
 */

(function () {
  'use strict';

  if (typeof Chart === 'undefined' || !window.__PAISATRACK_ANALYTICS__) {
    return;
  }

  const initialData = window.__PAISATRACK_ANALYTICS__;

  const categoryCanvas = document.getElementById('categoryDoughnutChart');
  const monthlyCanvas = document.getElementById('monthlyBarChart');
  const categoryEmpty = document.getElementById('category-empty-state');
  const monthlyEmpty = document.getElementById('monthly-empty-state');
  const breakdownList = document.getElementById('category-breakdown-list');
  const monthInput = document.getElementById('analytics-month-filter');
  const allTimeCheckbox = document.getElementById('analytics-all-time');
  const yearSelect = document.getElementById('analytics-year-filter');

  const kpiIncome = document.getElementById('analytics-kpi-income');
  const kpiExpense = document.getElementById('analytics-kpi-expense');
  const kpiNet = document.getElementById('analytics-kpi-net');

  const CHART_COLORS = [
    '#14532D',
    '#15803D',
    '#22C55E',
    '#86EFAC',
    '#0F766E',
    '#0284C7',
    '#D97706',
    '#B91C1C'
  ];

  let doughnutChart = null;
  let barChart = null;

  function formatIndianRupees(val) {
    const num = Number(val || 0);
    const negative = num < 0;
    const abs = Math.abs(num).toFixed(2);
    const [intPart, decPart] = abs.split('.');
    let grouped = intPart;
    if (intPart.length > 3) {
      const last3 = intPart.slice(-3);
      const rest = intPart.slice(0, -3).replace(/\B(?=(\d{2})+(?!\d))/g, ',');
      grouped = rest + ',' + last3;
    }
    return (negative ? '-₹' : '₹') + grouped + '.' + decPart;
  }

  function renderCategoryChart(items) {
    if (!categoryCanvas) return;

    if (!items || items.length === 0) {
      categoryCanvas.hidden = true;
      if (categoryEmpty) categoryEmpty.hidden = false;
      if (breakdownList) breakdownList.innerHTML = '';
      if (doughnutChart) {
        doughnutChart.destroy();
        doughnutChart = null;
      }
      return;
    }

    categoryCanvas.hidden = false;
    if (categoryEmpty) categoryEmpty.hidden = true;

    const labels = items.map((i) => i.categoryName);
    const values = items.map((i) => Number(i.totalAmount));
    const colors = items.map((_, idx) => CHART_COLORS[idx % CHART_COLORS.length]);

    if (doughnutChart) {
      doughnutChart.destroy();
    }

    doughnutChart = new Chart(categoryCanvas.getContext('2d'), {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [
          {
            data: values,
            backgroundColor: colors,
            borderColor: '#FFFFFF',
            borderWidth: 2
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '62%',
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              boxWidth: 12,
              padding: 14,
              font: {
                family: "'Plus Jakarta Sans', sans-serif",
                size: 12
              }
            }
          },
          tooltip: {
            callbacks: {
              label: function (ctx) {
                const item = items[ctx.dataIndex];
                return `${item.categoryName}: ${item.formattedAmount} (${item.percentage}%)`;
              }
            }
          }
        }
      }
    });

    if (breakdownList) {
      breakdownList.innerHTML = items
        .map((item, idx) => {
          const color = CHART_COLORS[idx % CHART_COLORS.length];
          return `
            <div class="breakdown-item">
              <div class="breakdown-row">
                <span><strong>${escapeHtml(item.categoryName)}</strong> · ${item.percentage}%</span>
                <span class="tabular-nums"><strong>${escapeHtml(item.formattedAmount)}</strong></span>
              </div>
              <div class="breakdown-bar-track">
                <div class="breakdown-bar-fill" style="width: ${Math.min(100, item.percentage)}%; background: ${color};"></div>
              </div>
            </div>
          `;
        })
        .join('');
    }
  }

  function renderMonthlyBarChart(monthlyItems) {
    if (!monthlyCanvas || !monthlyItems) return;

    const labels = monthlyItems.map((m) => m.monthLabel);
    const incomeValues = monthlyItems.map((m) => Number(m.income));
    const expenseValues = monthlyItems.map((m) => Number(m.expense));

    const hasAnyValue = incomeValues.some((v) => v > 0) || expenseValues.some((v) => v > 0);
    if (!hasAnyValue) {
      monthlyCanvas.hidden = true;
      if (monthlyEmpty) monthlyEmpty.hidden = false;
      if (barChart) {
        barChart.destroy();
        barChart = null;
      }
      return;
    }

    monthlyCanvas.hidden = false;
    if (monthlyEmpty) monthlyEmpty.hidden = true;

    if (barChart) {
      barChart.destroy();
    }

    barChart = new Chart(monthlyCanvas.getContext('2d'), {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [
          {
            label: 'Income',
            data: incomeValues,
            backgroundColor: '#15803D',
            borderRadius: 6
          },
          {
            label: 'Expense',
            data: expenseValues,
            backgroundColor: '#B91C1C',
            borderRadius: 6
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: function (value) {
                return formatIndianRupees(value);
              }
            },
            grid: {
              color: '#F1F5F9'
            }
          },
          x: {
            grid: {
              display: false
            }
          }
        },
        plugins: {
          legend: {
            position: 'bottom'
          },
          tooltip: {
            callbacks: {
              label: function (ctx) {
                return `${ctx.dataset.label}: ${formatIndianRupees(ctx.parsed.y)}`;
              }
            }
          }
        }
      }
    });
  }

  function applyAnalyticsData(data) {
    if (kpiIncome) kpiIncome.textContent = data.formattedTotalIncome;
    if (kpiExpense) kpiExpense.textContent = data.formattedTotalExpense;
    if (kpiNet) kpiNet.textContent = data.formattedNetBalance;

    renderCategoryChart(data.categoryBreakdown);
    renderMonthlyBarChart(data.monthlyComparison);
  }

  async function refreshAnalyticsFromServer() {
    const monthParam = allTimeCheckbox && allTimeCheckbox.checked
      ? 'ALL'
      : (monthInput && monthInput.value ? monthInput.value : '');
    const yearParam = yearSelect ? yearSelect.value : '';

    const params = new URLSearchParams();
    if (monthParam) params.set('month', monthParam);
    if (yearParam) params.set('year', yearParam);

    try {
      const res = await fetch('/api/analytics/data?' + params.toString(), {
        headers: { 'Accept': 'application/json' }
      });
      if (!res.ok || res.redirected) {
        if (res.status === 401) {
          window.location.href = '/login';
        }
        return;
      }
      const updated = await res.json();
      applyAnalyticsData(updated);
    } catch (err) {
      // Network offline or transient error
    }
  }

  function escapeHtml(str) {
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  if (monthInput) {
    monthInput.addEventListener('change', () => {
      if (allTimeCheckbox) allTimeCheckbox.checked = false;
      refreshAnalyticsFromServer();
    });
  }

  if (allTimeCheckbox) {
    allTimeCheckbox.addEventListener('change', () => {
      if (monthInput) monthInput.disabled = allTimeCheckbox.checked;
      refreshAnalyticsFromServer();
    });
    if (monthInput) {
      monthInput.disabled = allTimeCheckbox.checked;
    }
  }

  if (yearSelect) {
    yearSelect.addEventListener('change', refreshAnalyticsFromServer);
  }

  applyAnalyticsData(initialData);
})();
