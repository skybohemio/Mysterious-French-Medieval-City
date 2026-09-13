using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace BourgesAdminBackend.Models
{
    public class Site
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }

        [Required]
        [StringLength(150)]
        public string Title { get; set; } = string.Empty;

        [Required]
        public string Description { get; set; } = string.Empty;

        [Required]
        public string NarrationText { get; set; } = string.Empty;

        [Required]
        public double Latitude { get; set; }

        [Required]
        public double Longitude { get; set; }

        [Required]
        [StringLength(50)]
        public string Category { get; set; } = "CUSTOM"; // "CATHEDRAL", "PALACE", "NATURE", "MUSEUM", "CUSTOM"

        public bool IsPreset { get; set; } = false;

        public int AudioDurationSec { get; set; } = 60;

        // Localized Titles
        public string TitleFr { get; set; } = string.Empty;
        public string TitleEn { get; set; } = string.Empty;
        public string TitleDe { get; set; } = string.Empty;
        public string TitleEs { get; set; } = string.Empty;
        public string TitleNl { get; set; } = string.Empty;

        // Localized Descriptions
        public string DescriptionFr { get; set; } = string.Empty;
        public string DescriptionEn { get; set; } = string.Empty;
        public string DescriptionDe { get; set; } = string.Empty;
        public string DescriptionEs { get; set; } = string.Empty;
        public string DescriptionNl { get; set; } = string.Empty;

        // Localized Narrations
        public string NarrationFr { get; set; } = string.Empty;
        public string NarrationEn { get; set; } = string.Empty;
        public string NarrationDe { get; set; } = string.Empty;
        public string NarrationEs { get; set; } = string.Empty;
        public string NarrationNl { get; set; } = string.Empty;
    }
}
